package com.essenza.aws.modules.amazonSNS.application.output.repository;

import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.CreateSnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.UpdateSnsSubscriptionDto;

import java.util.List;
import java.util.Optional;

public interface SnsSubscriptionRepository {
    SnsSubscriptionDto create(CreateSnsSubscriptionDto input);

    SnsSubscriptionDto update(Long id, UpdateSnsSubscriptionDto input);

    boolean deleteById(Long id);

    Optional<SnsSubscriptionDto> findById(Long id);

    List<SnsSubscriptionDto> findAll();

    Optional<SnsSubscriptionDto> findBySubscriptionArn(String subscriptionArn);

    List<SnsSubscriptionDto> findByTopicArn(String topicArn);
}
