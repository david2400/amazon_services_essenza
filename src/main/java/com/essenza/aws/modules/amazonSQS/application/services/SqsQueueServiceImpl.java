package com.essenza.aws.modules.amazonSQS.application.services;

import com.essenza.aws.modules.amazonSQS.application.input.queues.CreateSqsQueueUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.queues.DeleteSqsQueueUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.queues.FindSqsQueueByIdUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.queues.FindSqsQueueByUrlUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.queues.FindSqsQueuesUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.queues.UpdateSqsQueueUseCase;
import com.essenza.aws.modules.amazonSQS.application.output.repository.SqsQueueRepository;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.CreateSqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueConstraints;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.UpdateSqsQueueDto;
import com.essenza.aws.shared.exceptions.AwsServiceException;
import com.essenza.aws.shared.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;
import software.amazon.awssdk.services.sqs.model.TagQueueRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsQueueServiceImpl implements
        CreateSqsQueueUseCase,
        UpdateSqsQueueUseCase,
        DeleteSqsQueueUseCase,
        FindSqsQueueByIdUseCase,
        FindSqsQueuesUseCase,
        FindSqsQueueByUrlUseCase {

    private static final String ATTR_DELAY_SECONDS = "DelaySeconds";
    private static final String ATTR_MAXIMUM_MESSAGE_SIZE = "MaximumMessageSize";
    private static final String ATTR_MESSAGE_RETENTION_PERIOD = "MessageRetentionPeriod";
    private static final String ATTR_VISIBILITY_TIMEOUT = "VisibilityTimeout";
    private static final String ATTR_FIFO_QUEUE = "FifoQueue";
    private static final String ATTR_CONTENT_BASED_DEDUPLICATION = "ContentBasedDeduplication";
    private static final String ATTR_REDRIVE_POLICY = "RedrivePolicy";

    /** AWS no permite modificar estos atributos después de crear la cola. */
    private static final Set<String> IMMUTABLE_ATTRIBUTES = Set.of(ATTR_FIFO_QUEUE, ATTR_CONTENT_BASED_DEDUPLICATION);

    private final SqsQueueRepository repository;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Override
    public SqsQueueDto create(CreateSqsQueueDto input) {
        boolean fifo = resolveFifo(input);
        validateRedrivePolicy(input.getRedrivePolicy());

        Map<String, String> attributes = buildAttributes(input, fifo);
        CreateQueueRequest.Builder requestBuilder = CreateQueueRequest.builder()
                .queueName(input.getName());
        if (!attributes.isEmpty()) {
            requestBuilder.attributesWithStrings(attributes);
        }
        if (!CollectionUtils.isEmpty(input.getTags())) {
            requestBuilder.tags(input.getTags());
        }

        String queueUrl;
        String queueArn;
        try {
            queueUrl = sqsClient.createQueue(requestBuilder.build()).queueUrl();
            queueArn = sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder()
                            .queueUrl(queueUrl)
                            .attributeNames(QueueAttributeName.QUEUE_ARN)
                            .build())
                    .attributes()
                    .get(QueueAttributeName.QUEUE_ARN);
        } catch (SqsException e) {
            log.error("Failed to create SQS queue {}: {}", input.getName(), e.awsErrorDetails().errorMessage(), e);
            throw new AwsServiceException("Failed to create SQS queue: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }
        log.info("SQS queue created: {} ({})", input.getName(), queueUrl);

        // CreateQueue es idempotente en AWS: si la cola ya existía con los mismos atributos devuelve la misma URL.
        Optional<SqsQueueDto> existing = repository.findByQueueUrl(queueUrl);
        if (existing.isPresent()) {
            log.info("SQS queue {} already registered with id {}", queueUrl, existing.get().getId());
            return existing.get();
        }

        CreateSqsQueueDto payload = CreateSqsQueueDto.builder()
                .name(input.getName())
                .queueUrl(queueUrl)
                .arn(queueArn)
                .delaySeconds(input.getDelaySeconds())
                .maximumMessageSize(input.getMaximumMessageSize())
                .messageRetentionPeriod(input.getMessageRetentionPeriod())
                .visibilityTimeout(input.getVisibilityTimeout())
                .fifoQueue(fifo)
                .contentBasedDeduplication(Boolean.TRUE.equals(input.getContentBasedDeduplication()))
                .redrivePolicy(input.getRedrivePolicy())
                .attributes(input.getAttributes())
                .tags(input.getTags())
                .status(StringUtils.hasText(input.getStatus()) ? input.getStatus() : "ACTIVE")
                .build();

        return repository.create(payload);
    }

    @Override
    public SqsQueueDto update(Long id, UpdateSqsQueueDto input) {
        SqsQueueDto current = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("SQS Queue not found with id " + id));

        validateImmutableAttributes(input.getAttributes());
        validateRedrivePolicy(input.getRedrivePolicy());

        Map<String, String> attributes = buildUpdateAttributes(input);
        try {
            if (!attributes.isEmpty()) {
                sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
                        .queueUrl(current.getQueueUrl())
                        .attributesWithStrings(attributes)
                        .build());
            }
            if (!CollectionUtils.isEmpty(input.getTags())) {
                sqsClient.tagQueue(TagQueueRequest.builder()
                        .queueUrl(current.getQueueUrl())
                        .tags(input.getTags())
                        .build());
            }
        } catch (SqsException e) {
            log.error("Failed to update SQS queue {}: {}", current.getQueueUrl(), e.awsErrorDetails().errorMessage(), e);
            throw new AwsServiceException("Failed to update SQS queue: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }
        log.info("SQS queue updated: {}", current.getQueueUrl());

        return repository.update(id, input);
    }

    @Override
    public boolean delete(Long id) {
        SqsQueueDto current = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("SQS Queue not found with id " + id));
        try {
            sqsClient.deleteQueue(DeleteQueueRequest.builder().queueUrl(current.getQueueUrl()).build());
        } catch (SqsException e) {
            log.error("Failed to delete SQS queue {}: {}", current.getQueueUrl(), e.awsErrorDetails().errorMessage(), e);
            throw new AwsServiceException("Failed to delete SQS queue: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }
        log.info("SQS queue deleted: {}", current.getQueueUrl());
        return repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SqsQueueDto> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SqsQueueDto> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SqsQueueDto> findByQueueUrl(String queueUrl) {
        return repository.findByQueueUrl(queueUrl);
    }

    /**
     * Determina si la cola es FIFO y valida la coherencia entre el nombre y los flags FIFO.
     */
    private boolean resolveFifo(CreateSqsQueueDto input) {
        boolean fifoName = SqsQueueConstraints.isFifo(input.getName());
        Map<String, String> extra = input.getAttributes();
        boolean fifoFlag = Boolean.TRUE.equals(input.getFifoQueue())
                || (extra != null && "true".equalsIgnoreCase(extra.get(ATTR_FIFO_QUEUE)));
        boolean contentDedup = Boolean.TRUE.equals(input.getContentBasedDeduplication())
                || (extra != null && "true".equalsIgnoreCase(extra.get(ATTR_CONTENT_BASED_DEDUPLICATION)));

        if (fifoFlag && !fifoName) {
            throw new IllegalArgumentException("FIFO queue names must end with " + SqsQueueConstraints.FIFO_SUFFIX);
        }
        if (fifoName && Boolean.FALSE.equals(input.getFifoQueue())) {
            throw new IllegalArgumentException("Queue names ending with " + SqsQueueConstraints.FIFO_SUFFIX
                    + " are FIFO queues: fifoQueue cannot be false");
        }
        if (contentDedup && !fifoName) {
            throw new IllegalArgumentException("contentBasedDeduplication is only valid for FIFO queues");
        }
        return fifoName;
    }

    private Map<String, String> buildAttributes(CreateSqsQueueDto input, boolean fifo) {
        Map<String, String> attributes = new HashMap<>();
        if (!CollectionUtils.isEmpty(input.getAttributes())) {
            attributes.putAll(input.getAttributes());
        }
        putIfPresent(attributes, ATTR_DELAY_SECONDS, input.getDelaySeconds());
        putIfPresent(attributes, ATTR_MAXIMUM_MESSAGE_SIZE, input.getMaximumMessageSize());
        putIfPresent(attributes, ATTR_MESSAGE_RETENTION_PERIOD, input.getMessageRetentionPeriod());
        putIfPresent(attributes, ATTR_VISIBILITY_TIMEOUT, input.getVisibilityTimeout());
        if (StringUtils.hasText(input.getRedrivePolicy())) {
            attributes.put(ATTR_REDRIVE_POLICY, input.getRedrivePolicy());
        }
        if (fifo) {
            attributes.put(ATTR_FIFO_QUEUE, "true");
        } else {
            // AWS rechaza FifoQueue=false explícito en colas estándar.
            attributes.remove(ATTR_FIFO_QUEUE);
        }
        if (Boolean.TRUE.equals(input.getContentBasedDeduplication())) {
            attributes.put(ATTR_CONTENT_BASED_DEDUPLICATION, "true");
        }
        return attributes;
    }

    private Map<String, String> buildUpdateAttributes(UpdateSqsQueueDto input) {
        Map<String, String> attributes = new HashMap<>();
        if (!CollectionUtils.isEmpty(input.getAttributes())) {
            attributes.putAll(input.getAttributes());
        }
        putIfPresent(attributes, ATTR_DELAY_SECONDS, input.getDelaySeconds());
        putIfPresent(attributes, ATTR_MAXIMUM_MESSAGE_SIZE, input.getMaximumMessageSize());
        putIfPresent(attributes, ATTR_MESSAGE_RETENTION_PERIOD, input.getMessageRetentionPeriod());
        putIfPresent(attributes, ATTR_VISIBILITY_TIMEOUT, input.getVisibilityTimeout());
        if (StringUtils.hasText(input.getRedrivePolicy())) {
            attributes.put(ATTR_REDRIVE_POLICY, input.getRedrivePolicy());
        }
        return attributes;
    }

    private void validateImmutableAttributes(Map<String, String> attributes) {
        if (CollectionUtils.isEmpty(attributes)) {
            return;
        }
        attributes.keySet().stream()
                .filter(IMMUTABLE_ATTRIBUTES::contains)
                .findFirst()
                .ifPresent(name -> {
                    throw new IllegalArgumentException(name + " cannot be updated after the queue is created");
                });
    }

    private void validateRedrivePolicy(String redrivePolicy) {
        if (!StringUtils.hasText(redrivePolicy)) {
            return;
        }
        JsonNode node;
        try {
            node = objectMapper.readTree(redrivePolicy);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid RedrivePolicy: must be a valid JSON document");
        }
        if (node == null || !node.isObject()
                || !node.hasNonNull("deadLetterTargetArn")
                || !node.hasNonNull("maxReceiveCount")) {
            throw new IllegalArgumentException(
                    "Invalid RedrivePolicy: deadLetterTargetArn and maxReceiveCount are required");
        }
        if (!node.get("deadLetterTargetArn").asText().startsWith("arn:aws")) {
            throw new IllegalArgumentException("Invalid RedrivePolicy: deadLetterTargetArn must be an SQS queue ARN");
        }
        int maxReceiveCount = node.get("maxReceiveCount").asInt(0);
        if (maxReceiveCount < 1 || maxReceiveCount > 1000) {
            throw new IllegalArgumentException("Invalid RedrivePolicy: maxReceiveCount must be between 1 and 1000");
        }
    }

    private static void putIfPresent(Map<String, String> attributes, String name, Integer value) {
        if (value != null) {
            attributes.put(name, String.valueOf(value));
        }
    }
}
