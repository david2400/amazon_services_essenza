package com.essenza.aws.modules.amazonSQS.application.services;

import com.essenza.aws.modules.amazonSQS.application.input.messages.DeleteSqsMessageUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.messages.FindSqsMessagesByQueueUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.messages.FindSqsMessagesUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.messages.ReceiveSqsMessagesUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.messages.SendSqsMessageBatchUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.messages.SendSqsMessageUseCase;
import com.essenza.aws.modules.amazonSQS.application.output.repository.SqsMessageRepository;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.DeleteSqsMessageDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.ReceiveSqsMessagesDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SendSqsMessageDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SqsMessageDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueConstraints;
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
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.BatchResultErrorEntry;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResultEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsMessageServiceImpl implements
        SendSqsMessageUseCase,
        SendSqsMessageBatchUseCase,
        ReceiveSqsMessagesUseCase,
        DeleteSqsMessageUseCase,
        FindSqsMessagesUseCase,
        FindSqsMessagesByQueueUseCase {

    private static final int SQS_BATCH_MAX_ENTRIES = 10;
    private static final int MAX_BATCH_SIZE = 100;

    private static final String OPERATION_SEND = "SEND";
    private static final String OPERATION_RECEIVE = "RECEIVE";
    private static final String OPERATION_DELETE = "DELETE";

    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_RECEIVED = "RECEIVED";
    private static final String STATUS_DELETED = "DELETED";
    private static final String STATUS_FAILED = "FAILED";

    private static final String METRIC_SENT = "sqs.messages.sent";
    private static final String METRIC_RECEIVED = "sqs.messages.received";
    private static final String METRIC_DELETED = "sqs.messages.deleted";

    private final SqsMessageRepository repository;
    private final SqsClient sqsClient;
    private final MeterRegistry meterRegistry;

    // ---------------------------------------------------------------- send

    @Override
    public SqsMessageDto send(SendSqsMessageDto input) {
        validateFifoParams(input);
        SqsMessageDto dto = baseMessage(input);
        try {
            SendMessageResponse response = sqsClient.sendMessage(buildSendRequest(input));
            dto.setMessageId(response.messageId());
            dto.setMd5OfBody(response.md5OfMessageBody());
            dto.setSequenceNumber(response.sequenceNumber());
            dto.setStatus(StringUtils.hasText(response.messageId()) ? STATUS_SENT : STATUS_FAILED);
            meterRegistry.counter(METRIC_SENT, "result", "success").increment();
            log.info("SQS message sent to {} with messageId {}", input.getQueueUrl(), response.messageId());
        } catch (SqsException e) {
            meterRegistry.counter(METRIC_SENT, "result", "error").increment();
            log.error("SQS send failed for queue {}: {}", input.getQueueUrl(), e.awsErrorDetails().errorMessage(), e);
            dto.setStatus(STATUS_FAILED);
            dto.setErrorMessage(e.awsErrorDetails().errorMessage());
            repository.save(dto);
            throw new AwsServiceException("Failed to send SQS message: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }
        return repository.save(dto);
    }

    @Override
    public List<SqsMessageDto> sendBatch(List<SendSqsMessageDto> inputs) {
        if (CollectionUtils.isEmpty(inputs) || inputs.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("Batch must contain between 1 and " + MAX_BATCH_SIZE + " messages");
        }
        inputs.forEach(this::validateFifoParams);

        // SendMessageBatch opera sobre una sola cola: agrupar por queueUrl (manteniendo el orden) y luego partir en chunks de 10.
        Map<String, List<SendSqsMessageDto>> byQueue = inputs.stream()
                .collect(Collectors.groupingBy(SendSqsMessageDto::getQueueUrl, LinkedHashMap::new, Collectors.toList()));

        List<SqsMessageDto> results = new ArrayList<>();
        for (List<SendSqsMessageDto> queueMessages : byQueue.values()) {
            for (int i = 0; i < queueMessages.size(); i += SQS_BATCH_MAX_ENTRIES) {
                results.addAll(sendChunk(queueMessages.subList(i, Math.min(i + SQS_BATCH_MAX_ENTRIES, queueMessages.size()))));
            }
        }
        return results;
    }

    private List<SqsMessageDto> sendChunk(List<SendSqsMessageDto> chunk) {
        List<SendMessageBatchRequestEntry> entries = IntStream.range(0, chunk.size())
                .mapToObj(i -> {
                    SendSqsMessageDto input = chunk.get(i);
                    SendMessageBatchRequestEntry.Builder entry = SendMessageBatchRequestEntry.builder()
                            .id(String.valueOf(i))
                            .messageBody(input.getMessageBody());
                    if (input.getDelaySeconds() != null) {
                        entry.delaySeconds(input.getDelaySeconds());
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

        String queueUrl = chunk.get(0).getQueueUrl();
        try {
            SendMessageBatchResponse response = sqsClient.sendMessageBatch(SendMessageBatchRequest.builder()
                    .queueUrl(queueUrl)
                    .entries(entries)
                    .build());

            Map<String, SendMessageBatchResultEntry> succeeded = response.successful().stream()
                    .collect(Collectors.toMap(SendMessageBatchResultEntry::id, Function.identity()));
            Map<String, BatchResultErrorEntry> failed = response.failed().stream()
                    .collect(Collectors.toMap(BatchResultErrorEntry::id, Function.identity()));

            List<SqsMessageDto> results = new ArrayList<>();
            for (int i = 0; i < chunk.size(); i++) {
                SqsMessageDto dto = baseMessage(chunk.get(i));
                String key = String.valueOf(i);
                if (succeeded.containsKey(key)) {
                    SendMessageBatchResultEntry ok = succeeded.get(key);
                    dto.setStatus(STATUS_SENT);
                    dto.setMessageId(ok.messageId());
                    dto.setMd5OfBody(ok.md5OfMessageBody());
                    dto.setSequenceNumber(ok.sequenceNumber());
                    meterRegistry.counter(METRIC_SENT, "result", "success").increment();
                } else {
                    dto.setStatus(STATUS_FAILED);
                    BatchResultErrorEntry error = failed.get(key);
                    dto.setErrorMessage(error != null ? error.code() + ": " + error.message() : "Unknown batch error");
                    meterRegistry.counter(METRIC_SENT, "result", "error").increment();
                }
                results.add(repository.save(dto));
            }
            return results;
        } catch (SqsException e) {
            meterRegistry.counter(METRIC_SENT, "result", "error").increment();
            log.error("SQS batch send failed for queue {}: {}", queueUrl, e.awsErrorDetails().errorMessage(), e);
            chunk.forEach(input -> {
                SqsMessageDto dto = baseMessage(input);
                dto.setStatus(STATUS_FAILED);
                dto.setErrorMessage(e.awsErrorDetails().errorMessage());
                repository.save(dto);
            });
            throw new AwsServiceException("Failed to send SQS batch: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }
    }

    // ------------------------------------------------------------- receive

    @Override
    public List<SqsMessageDto> receive(ReceiveSqsMessagesDto input) {
        ReceiveMessageRequest.Builder request = ReceiveMessageRequest.builder()
                .queueUrl(input.getQueueUrl())
                .maxNumberOfMessages(input.getMaxNumberOfMessages() != null ? input.getMaxNumberOfMessages() : 1)
                .attributeNames(QueueAttributeName.ALL)
                .messageAttributeNames("All");
        if (input.getWaitTimeSeconds() != null) {
            request.waitTimeSeconds(input.getWaitTimeSeconds());
        }
        if (input.getVisibilityTimeout() != null) {
            request.visibilityTimeout(input.getVisibilityTimeout());
        }

        ReceiveMessageResponse response;
        try {
            response = sqsClient.receiveMessage(request.build());
        } catch (SqsException e) {
            meterRegistry.counter(METRIC_RECEIVED, "result", "error").increment();
            log.error("SQS receive failed for queue {}: {}", input.getQueueUrl(), e.awsErrorDetails().errorMessage(), e);
            repository.save(SqsMessageDto.builder()
                    .operation(OPERATION_RECEIVE)
                    .queueUrl(input.getQueueUrl())
                    .status(STATUS_FAILED)
                    .errorMessage(e.awsErrorDetails().errorMessage())
                    .build());
            throw new AwsServiceException("Failed to receive SQS messages: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }

        List<SqsMessageDto> results = response.messages().stream()
                .map(message -> repository.save(toReceivedMessage(input.getQueueUrl(), message)))
                .toList();
        meterRegistry.counter(METRIC_RECEIVED, "result", "success").increment(results.size());
        log.info("SQS received {} message(s) from {}", results.size(), input.getQueueUrl());
        return results;
    }

    private SqsMessageDto toReceivedMessage(String queueUrl, Message message) {
        Map<MessageSystemAttributeName, String> system = message.attributes();
        return SqsMessageDto.builder()
                .operation(OPERATION_RECEIVE)
                .queueUrl(queueUrl)
                .messageId(message.messageId())
                .receiptHandle(message.receiptHandle())
                .messageBody(message.body())
                .md5OfBody(message.md5OfBody())
                .messageAttributes(unmapAttributes(message.messageAttributes()))
                .messageGroupId(system.get(MessageSystemAttributeName.MESSAGE_GROUP_ID))
                .messageDeduplicationId(system.get(MessageSystemAttributeName.MESSAGE_DEDUPLICATION_ID))
                .sequenceNumber(system.get(MessageSystemAttributeName.SEQUENCE_NUMBER))
                .status(STATUS_RECEIVED)
                .build();
    }

    // -------------------------------------------------------------- delete

    @Override
    public SqsMessageDto delete(DeleteSqsMessageDto input) {
        SqsMessageDto dto = SqsMessageDto.builder()
                .operation(OPERATION_DELETE)
                .queueUrl(input.getQueueUrl())
                .receiptHandle(input.getReceiptHandle())
                .messageId(input.getMessageId())
                .build();
        try {
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(input.getQueueUrl())
                    .receiptHandle(input.getReceiptHandle())
                    .build());
            dto.setStatus(STATUS_DELETED);
            meterRegistry.counter(METRIC_DELETED, "result", "success").increment();
            log.info("SQS message deleted from {}", input.getQueueUrl());
        } catch (SqsException e) {
            meterRegistry.counter(METRIC_DELETED, "result", "error").increment();
            log.error("SQS delete failed for queue {}: {}", input.getQueueUrl(), e.awsErrorDetails().errorMessage(), e);
            dto.setStatus(STATUS_FAILED);
            dto.setErrorMessage(e.awsErrorDetails().errorMessage());
            repository.save(dto);
            throw new AwsServiceException("Failed to delete SQS message: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }
        return repository.save(dto);
    }

    // --------------------------------------------------------------- reads

    @Override
    @Transactional(readOnly = true)
    public Page<SqsMessageDto> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SqsMessageDto> findByQueueUrl(String queueUrl, Pageable pageable) {
        return repository.findByQueueUrl(queueUrl, pageable);
    }

    // ------------------------------------------------------------- helpers

    private SendMessageRequest buildSendRequest(SendSqsMessageDto input) {
        SendMessageRequest.Builder builder = SendMessageRequest.builder()
                .queueUrl(input.getQueueUrl())
                .messageBody(input.getMessageBody());

        if (input.getDelaySeconds() != null) {
            builder.delaySeconds(input.getDelaySeconds());
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

    private Map<String, String> unmapAttributes(Map<String, MessageAttributeValue> attributes) {
        if (CollectionUtils.isEmpty(attributes)) {
            return Map.of();
        }
        return attributes.entrySet().stream()
                .filter(entry -> entry.getValue().stringValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stringValue()));
    }

    private void validateFifoParams(SendSqsMessageDto input) {
        boolean fifo = SqsQueueConstraints.isFifo(input.getQueueUrl());
        if (!fifo && (StringUtils.hasText(input.getMessageGroupId())
                || StringUtils.hasText(input.getMessageDeduplicationId()))) {
            throw new IllegalArgumentException(
                    "messageGroupId and messageDeduplicationId are only valid for FIFO queues (URL must end with .fifo)");
        }
        if (fifo && !StringUtils.hasText(input.getMessageGroupId())) {
            throw new IllegalArgumentException("messageGroupId is required for FIFO queues");
        }
        if (fifo && input.getDelaySeconds() != null && !Objects.equals(input.getDelaySeconds(), 0)) {
            throw new IllegalArgumentException(
                    "Per-message delaySeconds is not supported on FIFO queues; set DelaySeconds on the queue instead");
        }
    }

    private SqsMessageDto baseMessage(SendSqsMessageDto input) {
        return SqsMessageDto.builder()
                .operation(OPERATION_SEND)
                .queueUrl(input.getQueueUrl())
                .messageBody(input.getMessageBody())
                .messageAttributes(input.getMessageAttributes())
                .messageGroupId(input.getMessageGroupId())
                .messageDeduplicationId(input.getMessageDeduplicationId())
                .delaySeconds(input.getDelaySeconds())
                .build();
    }
}
