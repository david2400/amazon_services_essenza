package com.essenza.aws.modules.amazonSNS.application.services;

import com.essenza.aws.modules.amazonSNS.application.input.topics.CreateSnsTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.DeleteSnsTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.FindSnsTopicByArnUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.FindSnsTopicByIdUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.FindSnsTopicsUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.UpdateSnsTopicUseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.CreateSnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.SnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.UpdateSnsTopicDto;
import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.topics.SnsTopicRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.DeleteTopicRequest;
import software.amazon.awssdk.services.sns.model.SetTopicAttributesRequest;
import software.amazon.awssdk.services.sns.model.Tag;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SnsTopicServiceImpl implements
        CreateSnsTopicUseCase,
        UpdateSnsTopicUseCase,
        DeleteSnsTopicUseCase,
        FindSnsTopicByIdUseCase,
        FindSnsTopicsUseCase,
        FindSnsTopicByArnUseCase {

    private final SnsTopicRepositoryAdapter repository;
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Override
    public SnsTopicDto create(CreateSnsTopicDto input) {
        Map<String, String> attributes = buildAttributes(input);
        CreateTopicRequest.Builder requestBuilder = CreateTopicRequest.builder()
                .name(input.getName());
        if (!attributes.isEmpty()) {
            requestBuilder.attributes(attributes);
        }
        List<Tag> tags = buildTags(input);
        if (!tags.isEmpty()) {
            requestBuilder.tags(tags);
        }
        var response = snsClient.createTopic(requestBuilder.build());

        CreateSnsTopicDto payload = CreateSnsTopicDto.builder()
                .name(input.getName())
                .displayName(input.getDisplayName())
                .arn(response.topicArn())
                .policy(input.getPolicy())
                .fifoTopic(input.getFifoTopic())
                .contentBasedDeduplication(input.getContentBasedDeduplication())
                .attributesJson(input.getAttributesJson())
                .tagsJson(input.getTagsJson())
                .status(StringUtils.hasText(input.getStatus()) ? input.getStatus() : "ACTIVE")
                .build();

        return repository.create(payload);
    }

    @Override
    public SnsTopicDto update(Long id, UpdateSnsTopicDto input) {
        SnsTopicDto current = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SNS Topic not found"));

        if (StringUtils.hasText(input.getDisplayName())) {
            setTopicAttribute(current.getArn(), "DisplayName", input.getDisplayName());
        }
        if (StringUtils.hasText(input.getPolicy())) {
            setTopicAttribute(current.getArn(), "Policy", input.getPolicy());
        }
        if (input.getFifoTopic() != null) {
            setTopicAttribute(current.getArn(), "FifoTopic", input.getFifoTopic().toString());
        }
        if (input.getContentBasedDeduplication() != null) {
            setTopicAttribute(current.getArn(), "ContentBasedDeduplication", input.getContentBasedDeduplication().toString());
        }

        return repository.update(id, input);
    }

    @Override
    public boolean delete(Long id) {
        SnsTopicDto current = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SNS Topic not found"));
        snsClient.deleteTopic(DeleteTopicRequest.builder().topicArn(current.getArn()).build());
        return repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SnsTopicDto> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SnsTopicDto> findAll() {
        return repository.findAll();
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
        attributes.putAll(parseJsonMap(input.getAttributesJson()));
        return attributes;
    }

    private List<Tag> buildTags(CreateSnsTopicDto input) {
        Map<String, String> tagsMap = parseJsonMap(input.getTagsJson());
        if (tagsMap.isEmpty()) {
            return Collections.emptyList();
        }
        return tagsMap.entrySet().stream()
                .map(entry -> Tag.builder().key(entry.getKey()).value(entry.getValue()).build())
                .toList();
    }

    private Map<String, String> parseJsonMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Error parsing SNS topic JSON payload", e);
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
