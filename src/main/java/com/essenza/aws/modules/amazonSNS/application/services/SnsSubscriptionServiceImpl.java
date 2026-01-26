package com.essenza.aws.modules.amazonSNS.application.services;

import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.CreateSnsSubscriptionUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.DeleteSnsSubscriptionUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.FindSnsSubscriptionByIdUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.FindSnsSubscriptionsByTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.FindSnsSubscriptionsUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.UpdateSnsSubscriptionUseCase;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.CreateSnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.UpdateSnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.subscriptions.SnsSubscriptionRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SetSubscriptionAttributesRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SnsSubscriptionServiceImpl implements
        CreateSnsSubscriptionUseCase,
        UpdateSnsSubscriptionUseCase,
        DeleteSnsSubscriptionUseCase,
        FindSnsSubscriptionByIdUseCase,
        FindSnsSubscriptionsUseCase,
        FindSnsSubscriptionsByTopicUseCase {

    private final SnsSubscriptionRepositoryAdapter repository;
    private final SnsClient snsClient;

    @Override
    public SnsSubscriptionDto subscribe(CreateSnsSubscriptionDto input) {
        SubscribeRequest.Builder requestBuilder = SubscribeRequest.builder()
                .topicArn(input.getTopicArn())
                .protocol(input.getProtocol())
                .endpoint(input.getEndpoint())
                .returnSubscriptionArn(true);

        java.util.Map<String, String> attributes = new java.util.HashMap<>();
        if (StringUtils.hasText(input.getFilterPolicy())) {
            attributes.put("FilterPolicy", input.getFilterPolicy());
        }
        if (Boolean.TRUE.equals(input.getRawMessageDelivery())) {
            attributes.put("RawMessageDelivery", "true");
        }
        if (!attributes.isEmpty()) {
            requestBuilder.attributes(attributes);
        }

        SubscribeResponse response = snsClient.subscribe(requestBuilder.build());

        CreateSnsSubscriptionDto payload = CreateSnsSubscriptionDto.builder()
                .topicArn(input.getTopicArn())
                .protocol(input.getProtocol())
                .endpoint(input.getEndpoint())
                .filterPolicy(input.getFilterPolicy())
                .rawMessageDelivery(input.getRawMessageDelivery())
                .subscriptionArn(response.subscriptionArn())
                .deliveryStatus("ACTIVE")
                .build();

        return repository.create(payload);
    }

    @Override
    public SnsSubscriptionDto update(Long id, UpdateSnsSubscriptionDto input) {
        SnsSubscriptionDto current = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SNS Subscription not found"));

        if (StringUtils.hasText(input.getFilterPolicy())) {
            setSubscriptionAttribute(current.getSubscriptionArn(), "FilterPolicy", input.getFilterPolicy());
        }
        if (input.getRawMessageDelivery() != null) {
            setSubscriptionAttribute(current.getSubscriptionArn(), "RawMessageDelivery", input.getRawMessageDelivery().toString());
        }

        return repository.update(id, input);
    }

    @Override
    public boolean unsubscribe(Long id) {
        SnsSubscriptionDto current = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SNS Subscription not found"));
        snsClient.unsubscribe(UnsubscribeRequest.builder().subscriptionArn(current.getSubscriptionArn()).build());
        return repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SnsSubscriptionDto> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SnsSubscriptionDto> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SnsSubscriptionDto> findByTopicArn(String topicArn) {
        return repository.findByTopicArn(topicArn);
    }

    private void setSubscriptionAttribute(String subscriptionArn, String attribute, String value) {
        snsClient.setSubscriptionAttributes(SetSubscriptionAttributesRequest.builder()
                .subscriptionArn(subscriptionArn)
                .attributeName(attribute)
                .attributeValue(value)
                .build());
    }
}
