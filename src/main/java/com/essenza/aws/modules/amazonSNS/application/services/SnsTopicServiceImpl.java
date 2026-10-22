package com.essenza.aws.modules.amazonSNS.application.services;

import com.essenza.aws.modules.amazonSNS.application.input.topics.CreateSnsTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.DeleteSnsTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.FindSnsTopicByArnUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.FindSnsTopicByIdUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.FindSnsTopicsUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.UpdateSnsTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.output.repository.SnsTopicRepository;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.CreateSnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.SnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.UpdateSnsTopicDto;
import com.essenza.aws.shared.exceptions.AwsServiceException;
import com.essenza.aws.shared.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.DeleteTopicRequest;
import software.amazon.awssdk.services.sns.model.SetTopicAttributesRequest;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.services.sns.model.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnsTopicServiceImpl implements
        CreateSnsTopicUseCase,
        UpdateSnsTopicUseCase,
        DeleteSnsTopicUseCase,
        FindSnsTopicByIdUseCase,
        FindSnsTopicsUseCase,
        FindSnsTopicByArnUseCase {

    private final SnsTopicRepository repository;
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Override
    public SnsTopicDto create(CreateSnsTopicDto input) {
        validatePolicy(input.getPolicy());
        CreateTopicRequest.Builder requestBuilder = CreateTopicRequest.builder()
                .name(input.getName());

        Map<String, String> attributes = buildAttributes(input);
        if (!attributes.isEmpty()) {
            requestBuilder.attributes(attributes);
        }
        List<Tag> tags = buildTags(input);
        if (!tags.isEmpty()) {
            requestBuilder.tags(tags);
        }

        String topicArn;
        try {
            topicArn = snsClient.createTopic(requestBuilder.build()).topicArn();
        } catch (SnsException e) {
            log.error("Failed to create SNS topic {}: {}", input.getName(), e.awsErrorDetails().errorMessage(), e);
            throw new AwsServiceException("Failed to create SNS topic: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }
        log.info("SNS topic created: {} ({})", input.getName(), topicArn);

        CreateSnsTopicDto payload = CreateSnsTopicDto.builder()
                .name(input.getName())
                .displayName(input.getDisplayName())
                .arn(topicArn)
                .policy(input.getPolicy())
                .fifoTopic(input.getFifoTopic())
                .contentBasedDeduplication(input.getContentBasedDeduplication())
                .attributes(input.getAttributes())
                .tags(input.getTags())
                .status(StringUtils.hasText(input.getStatus()) ? input.getStatus() : "ACTIVE")
                .build();

        return repository.create(payload);
    }

    @Override
    public SnsTopicDto update(Long id, UpdateSnsTopicDto input) {
        SnsTopicDto current = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("SNS Topic not found with id " + id));

        validatePolicy(input.getPolicy());
        try {
            if (StringUtils.hasText(input.getDisplayName())) {
                setTopicAttribute(current.getArn(), "DisplayName", input.getDisplayName());
            }
            if (StringUtils.hasText(input.getPolicy())) {
                setTopicAttribute(current.getArn(), "Policy", input.getPolicy());
            }
            if (!CollectionUtils.isEmpty(input.getAttributes())) {
                input.getAttributes()
                        .forEach((name, value) -> setTopicAttribute(current.getArn(), name, value));
            }
        } catch (SnsException e) {
            log.error("Failed to update SNS topic {}: {}", current.getArn(), e.awsErrorDetails().errorMessage(), e);
            throw new AwsServiceException("Failed to update SNS topic: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }

        return repository.update(id, input);
    }

    @Override
    public boolean delete(Long id) {
        SnsTopicDto current = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("SNS Topic not found with id " + id));
        try {
            snsClient.deleteTopic(DeleteTopicRequest.builder().topicArn(current.getArn()).build());
        } catch (SnsException e) {
            log.error("Failed to delete SNS topic {}: {}", current.getArn(), e.awsErrorDetails().errorMessage(), e);
            throw new AwsServiceException("Failed to delete SNS topic: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }
        log.info("SNS topic deleted: {}", current.getArn());
        return repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SnsTopicDto> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SnsTopicDto> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SnsTopicDto> findByArn(String arn) {
        return repository.findByArn(arn);
    }

    private Map<String, String> buildAttributes(CreateSnsTopicDto input) {
        Map<String, String> attributes = new HashMap<>();
        if (StringUtils.hasText(input.getDisplayName())) {
            attributes.put("DisplayName", input.getDisplayName());
        }
        if (StringUtils.hasText(input.getPolicy())) {
            attributes.put("Policy", input.getPolicy());
        }
        if (Boolean.TRUE.equals(input.getFifoTopic())) {
            attributes.put("FifoTopic", "true");
        }
        if (Boolean.TRUE.equals(input.getContentBasedDeduplication())) {
            attributes.put("ContentBasedDeduplication", "true");
        }
        if (!CollectionUtils.isEmpty(input.getAttributes())) {
            attributes.putAll(input.getAttributes());
        }
        return attributes;
    }

    private List<Tag> buildTags(CreateSnsTopicDto input) {
        if (CollectionUtils.isEmpty(input.getTags())) {
            return List.of();
        }
        return input.getTags().entrySet().stream()
                .map(entry -> Tag.builder().key(entry.getKey()).value(entry.getValue()).build())
                .toList();
    }

    private void validatePolicy(String policy) {
        if (!StringUtils.hasText(policy)) {
            return;
        }
        try {
            objectMapper.readTree(policy);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SNS topic policy: must be a valid IAM policy JSON document");
        }
    }

    private void setTopicAttribute(String topicArn, String name, String value) {
        snsClient.setTopicAttributes(SetTopicAttributesRequest.builder()
                .topicArn(topicArn)
                .attributeName(name)
                .attributeValue(value)
                .build());
    }
}
