package com.essenza.aws.modules.amazonSNS.application.input.subscriptions;

import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;

import java.util.Optional;

public interface FindSnsSubscriptionByIdUseCase {
    Optional<SnsSubscriptionDto> findById(Long id);
}
