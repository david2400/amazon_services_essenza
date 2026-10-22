package com.essenza.aws.modules.amazonSNS.application.services;

import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.CreateSnsSubscriptionUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.DeleteSnsSubscriptionUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.FindSnsSubscriptionByIdUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.FindSnsSubscriptionsByTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.FindSnsSubscriptionsUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.UpdateSnsSubscriptionUseCase;
import com.essenza.aws.modules.amazonSNS.application.output.repository.SnsSubscriptionRepository;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.CreateSnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.UpdateSnsSubscriptionDto;
import com.essenza.aws.shared.exceptions.AwsServiceException;
import com.essenza.aws.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SetSubscriptionAttributesRequest;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnsSubscriptionServiceImpl implements
        CreateSnsSubscriptionUseCase,
        UpdateSnsSubscriptionUseCase,
        DeleteSnsSubscriptionUseCase,
        FindSnsSubscriptionByIdUseCase,
        FindSnsSubscriptionsUseCase,
        FindSnsSubscriptionsByTopicUseCase {

    private static final String PENDING_CONFIRMATION = "pending confirmation";

    private final SnsSubscriptionRepository repository;
    private final SnsClient snsClient;

    @Override
    public SnsSubscriptionDto subscribe(CreateSnsSubscriptionDto input) {
        SubscribeRequest.Builder requestBuilder = SubscribeRequest.builder()
                .topicArn(input.getTopicArn())
                .protocol(input.getProtocol())
                .endpoint(input.getEndpoint())
                .returnSubscriptionArn(true);

        Map<String, String> attributes = new HashMap<>();
        if (StringUtils.hasText(input.getFilterPolicy())) {
            attributes.put("FilterPolicy", input.getFilterPolicy());
        }
        if (Boolean.TRUE.equals(input.getRawMessageDelivery())) {
            attributes.put("RawMessageDelivery", "true");
        }
        if (!attributes.isEmpty()) {
            requestBuilder.attributes(attributes);
        }

        SubscribeResponse response;
        try {
            response = snsClient.subscribe(requestBuilder.build());
        } catch (SnsException e) {
            log.error("Failed to subscribe {} to {}: {}", input.getEndpoint(), input.getTopicArn(),
                    e.awsErrorDetails().errorMessage(), e);
            throw new AwsServiceException("Failed to create SNS subscription: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }

        boolean pending = PENDING_CONFIRMATION.equalsIgnoreCase(response.subscriptionArn());
        log.info("SNS subscription created for {} (pending={})", input.getEndpoint(), pending);

        CreateSnsSubscriptionDto payload = CreateSnsSubscriptionDto.builder()
                .topicArn(input.getTopicArn())
                .protocol(input.getProtocol())
                .endpoint(input.getEndpoint())
                .filterPolicy(input.getFilterPolicy())
                .rawMessageDelivery(input.getRawMessageDelivery())
                .subscriptionArn(pending ? null : response.subscriptionArn())
                .deliveryStatus(pending ? "PENDING" : "ACTIVE")
                .build();

        return repository.create(payload);
    }

    @Override
    public SnsSubscriptionDto update(Long id, UpdateSnsSubscriptionDto input) {
        SnsSubscriptionDto current = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("SNS Subscription not found with id " + id));

        try {
            if (StringUtils.hasText(input.getFilterPolicy())) {
                setSubscriptionAttribute(current.getSubscriptionArn(), "FilterPolicy", input.getFilterPolicy());
            }
            if (input.getRawMessageDelivery() != null) {
                setSubscriptionAttribute(current.getSubscriptionArn(), "RawMessageDelivery",
                        input.getRawMessageDelivery().toString());
            }
        } catch (SnsException e) {
            log.error("Failed to update SNS subscription {}: {}", current.getSubscriptionArn(),
                    e.awsErrorDetails().errorMessage(), e);
            throw new AwsServiceException("Failed to update SNS subscription: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }

        return repository.update(id, input);
    }

    @Override
    public boolean unsubscribe(Long id) {
        SnsSubscriptionDto current = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("SNS Subscription not found with id " + id));
        try {
            snsClient.unsubscribe(UnsubscribeRequest.builder()
                    .subscriptionArn(current.getSubscriptionArn())
                    .build());
        } catch (SnsException e) {
            log.error("Failed to unsubscribe {}: {}", current.getSubscriptionArn(),
                    e.awsErrorDetails().errorMessage(), e);
            throw new AwsServiceException("Failed to delete SNS subscription: " + e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode(), e);
        }
        log.info("SNS subscription removed: {}", current.getSubscriptionArn());
        return repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SnsSubscriptionDto> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SnsSubscriptionDto> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SnsSubscriptionDto> findByTopicArn(String topicArn, Pageable pageable) {
        return repository.findByTopicArn(topicArn, pageable);
    }

    private void setSubscriptionAttribute(String subscriptionArn, String attribute, String value) {
        snsClient.setSubscriptionAttributes(SetSubscriptionAttributesRequest.builder()
                .subscriptionArn(subscriptionArn)
                .attributeName(attribute)
                .attributeValue(value)
                .build());
    }
}
