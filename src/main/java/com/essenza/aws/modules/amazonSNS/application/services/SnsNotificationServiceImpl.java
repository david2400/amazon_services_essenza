package com.essenza.aws.modules.amazonSNS.application.services;

import com.essenza.aws.modules.amazonSNS.application.input.notifications.FindSnsNotificationsByTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.notifications.FindSnsNotificationsUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.notifications.PublishSnsNotificationBatchUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.notifications.PublishSnsNotificationUseCase;
import com.essenza.aws.modules.amazonSNS.application.output.repository.SnsNotificationRepository;
import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.PublishSnsNotificationDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;
import com.essenza.aws.shared.exceptions.AwsServiceException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.BatchResultErrorEntry;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishBatchRequest;
import software.amazon.awssdk.services.sns.model.PublishBatchRequestEntry;
import software.amazon.awssdk.services.sns.model.PublishBatchResponse;
import software.amazon.awssdk.services.sns.model.PublishBatchResultEntry;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnsNotificationServiceImpl implements
        PublishSnsNotificationUseCase,
        PublishSnsNotificationBatchUseCase,
        FindSnsNotificationsUseCase,
        FindSnsNotificationsByTopicUseCase {

    private static final int SNS_BATCH_MAX_ENTRIES = 10;
    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_FAILED = "FAILED";

    private final SnsNotificationRepository repository;
    private final SnsClient snsClient;
    private final MeterRegistry meterRegistry;

    @Override
    public SnsNotificationDto publish(PublishSnsNotificationDto input) {
        validateFifoParams(input);
        SnsNotificationDto dto = baseNotification(input);
        try {
            PublishResponse response = snsClient.publish(buildPublishRequest(input));
            dto.setMessageId(response.messageId());
            dto.setStatus(StringUtils.hasText(response.messageId()) ? STATUS_SENT : STATUS_FAILED);
            meterRegistry.counter("sns.notifications.published", "result", "success").increment();
            log.info("SNS message published to {} with messageId {}", input.getTopicArn(), response.messageId());
        } catch (SnsException e) {
            meterRegistry.counter("sns.notifications.published", "result", "error").increment();
            log.error("SNS publish failed for topic {}: {}", input.getTopicArn(), e.awsErrorDetails().errorMessage(), e);
            dto.setStatus(STATUS_FAILED);
            dto.setErrorMessage(e.awsErrorDetails().errorMessage());
            repository.save(dto);
            throw new AwsServiceException("Failed to publish SNS notification: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }
        return repository.save(dto);
    }

    @Override
    public List<SnsNotificationDto> publishBatch(List<PublishSnsNotificationDto> inputs) {
        inputs.forEach(this::validateFifoParams);
        List<SnsNotificationDto> results = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i += SNS_BATCH_MAX_ENTRIES) {
            results.addAll(publishChunk(inputs.subList(i, Math.min(i + SNS_BATCH_MAX_ENTRIES, inputs.size()))));
        }
        return results;
    }

    private List<SnsNotificationDto> publishChunk(List<PublishSnsNotificationDto> chunk) {
        List<PublishBatchRequestEntry> entries = IntStream.range(0, chunk.size())
                .mapToObj(i -> {
                    PublishSnsNotificationDto input = chunk.get(i);
                    PublishBatchRequestEntry.Builder entry = PublishBatchRequestEntry.builder()
                            .id(String.valueOf(i))
                            .message(input.getMessage());
                    if (StringUtils.hasText(input.getSubject())) {
                        entry.subject(input.getSubject());
                    }
                    if (StringUtils.hasText(input.getMessageGroupId())) {
                        entry.messageGroupId(input.getMessageGroupId());
                    }
                    if (StringUtils.hasText(input.getMessageDeduplicationId())) {
                        entry.messageDeduplicationId(input.getMessageDeduplicationId());
                    }
                    Map<String, MessageAttributeValue> attributes = mapAttributes(input.getMessageAttributes());
                    if (!attributes.isEmpty()) {
                        entry.messageAttributes(attributes);
                    }
                    return entry.build();
                })
                .toList();

        String topicArn = chunk.get(0).getTopicArn();
        try {
            PublishBatchResponse response = snsClient.publishBatch(PublishBatchRequest.builder()
                    .topicArn(topicArn)
                    .publishBatchRequestEntries(entries)
                    .build());

            Map<String, PublishBatchResultEntry> succeeded = response.successful().stream()
                    .collect(Collectors.toMap(PublishBatchResultEntry::id, Function.identity()));
            Map<String, BatchResultErrorEntry> failed = response.failed().stream()
                    .collect(Collectors.toMap(BatchResultErrorEntry::id, Function.identity()));

            List<SnsNotificationDto> results = new ArrayList<>();
            for (int i = 0; i < chunk.size(); i++) {
                SnsNotificationDto dto = baseNotification(chunk.get(i));
                String key = String.valueOf(i);
                if (succeeded.containsKey(key)) {
                    dto.setStatus(STATUS_SENT);
                    dto.setMessageId(succeeded.get(key).messageId());
                    meterRegistry.counter("sns.notifications.published", "result", "success").increment();
                } else {
                    dto.setStatus(STATUS_FAILED);
                    BatchResultErrorEntry error = failed.get(key);
                    dto.setErrorMessage(error != null ? error.message() : "Unknown batch error");
                    meterRegistry.counter("sns.notifications.published", "result", "error").increment();
                }
                results.add(repository.save(dto));
            }
            return results;
        } catch (SnsException e) {
            meterRegistry.counter("sns.notifications.published", "result", "error").increment();
            log.error("SNS batch publish failed for topic {}: {}", topicArn, e.awsErrorDetails().errorMessage(), e);
            List<SnsNotificationDto> results = chunk.stream()
                    .map(input -> {
                        SnsNotificationDto dto = baseNotification(input);
                        dto.setStatus(STATUS_FAILED);
                        dto.setErrorMessage(e.awsErrorDetails().errorMessage());
                        return repository.save(dto);
                    })
                    .toList();
            throw new AwsServiceException("Failed to publish SNS batch: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }
    }

    private PublishRequest buildPublishRequest(PublishSnsNotificationDto input) {
        PublishRequest.Builder builder = PublishRequest.builder()
                .topicArn(input.getTopicArn())
                .message(input.getMessage());

        if (StringUtils.hasText(input.getSubject())) {
            builder.subject(input.getSubject());
        }
        if (StringUtils.hasText(input.getMessageGroupId())) {
            builder.messageGroupId(input.getMessageGroupId());
        }
        if (StringUtils.hasText(input.getMessageDeduplicationId())) {
            builder.messageDeduplicationId(input.getMessageDeduplicationId());
        }
        Map<String, MessageAttributeValue> attributes = mapAttributes(input.getMessageAttributes());
        if (!attributes.isEmpty()) {
            builder.messageAttributes(attributes);
        }
        return builder.build();
    }

    private Map<String, MessageAttributeValue> mapAttributes(Map<String, String> attributes) {
        if (CollectionUtils.isEmpty(attributes)) {
            return Map.of();
        }
        return attributes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue(entry.getValue())
                                .build()
                ));
    }

    private void validateFifoParams(PublishSnsNotificationDto input) {
        boolean fifo = input.getTopicArn() != null && input.getTopicArn().endsWith(".fifo");
        if (!fifo && (StringUtils.hasText(input.getMessageGroupId())
                || StringUtils.hasText(input.getMessageDeduplicationId()))) {
            throw new IllegalArgumentException(
                    "messageGroupId and messageDeduplicationId are only valid for FIFO topics (ARN must end with .fifo)");
        }
        if (fifo && !StringUtils.hasText(input.getMessageGroupId())) {
            throw new IllegalArgumentException("messageGroupId is required for FIFO topics");
        }
    }

    private SnsNotificationDto baseNotification(PublishSnsNotificationDto input) {
        return SnsNotificationDto.builder()
                .topicArn(input.getTopicArn())
                .subject(input.getSubject())
                .message(input.getMessage())
                .messageAttributes(input.getMessageAttributes())
                .messageGroupId(input.getMessageGroupId())
                .messageDeduplicationId(input.getMessageDeduplicationId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SnsNotificationDto> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SnsNotificationDto> findByTopicArn(String topicArn, Pageable pageable) {
        return repository.findByTopicArn(topicArn, pageable);
    }
}
