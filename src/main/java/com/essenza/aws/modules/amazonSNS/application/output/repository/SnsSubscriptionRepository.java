package com.essenza.aws.modules.amazonSNS.application.output.repository;

import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.CreateSnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.UpdateSnsSubscriptionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SnsSubscriptionRepository {
    SnsSubscriptionDto create(CreateSnsSubscriptionDto input);

    SnsSubscriptionDto update(Long id, UpdateSnsSubscriptionDto input);

    boolean deleteById(Long id);

    Optional<SnsSubscriptionDto> findById(Long id);

    Page<SnsSubscriptionDto> findAll(Pageable pageable);

    Optional<SnsSubscriptionDto> findBySubscriptionArn(String subscriptionArn);

    Page<SnsSubscriptionDto> findByTopicArn(String topicArn, Pageable pageable);
}
