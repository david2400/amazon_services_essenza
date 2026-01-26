package com.essenza.aws.modules.amazonSNS.application.services;

import com.essenza.aws.modules.amazonSNS.application.input.notifications.FindSnsNotificationsByTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.notifications.FindSnsNotificationsUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.notifications.PublishSnsNotificationUseCase;
import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.PublishSnsNotificationDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;
import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.notifications.SnsNotificationRepositoryAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SnsNotificationServiceImpl implements
        PublishSnsNotificationUseCase,
        FindSnsNotificationsUseCase,
        FindSnsNotificationsByTopicUseCase {

    private final SnsNotificationRepositoryAdapter repository;
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Override
    public SnsNotificationDto publish(PublishSnsNotificationDto input) {
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
        if (!CollectionUtils.isEmpty(input.getMessageAttributes())) {
            Map<String, MessageAttributeValue> mappedAttributes = input.getMessageAttributes().entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> MessageAttributeValue.builder()
                                    .dataType("String")
                                    .stringValue(entry.getValue())
                                    .build()
                    ));
            builder.messageAttributes(mappedAttributes);
        }

        PublishResponse publishResponse = snsClient.publish(builder.build());

        SnsNotificationDto dto = SnsNotificationDto.builder()
                .topicArn(input.getTopicArn())
                .subject(input.getSubject())
                .message(input.getMessage())
                .messageAttributesJson(toJson(input.getMessageAttributes()))
                .messageGroupId(input.getMessageGroupId())
                .messageDeduplicationId(input.getMessageDeduplicationId())
                .status(StringUtils.hasText(publishResponse.messageId()) ? "SENT" : "FAILED")
                .build();

        return repository.save(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<SnsNotificationDto> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<SnsNotificationDto> findByTopicArn(String topicArn) {
        return repository.findByTopicArn(topicArn);
    }

    private String toJson(Map<String, String> attributes) {
        if (CollectionUtils.isEmpty(attributes)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing SNS message attributes", e);
        }
    }
}
