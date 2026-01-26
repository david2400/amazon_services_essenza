package com.essenza.aws.modules.amazonSNS.application.input.subscriptions;

import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.UpdateSnsSubscriptionDto;

public interface UpdateSnsSubscriptionUseCase {
    SnsSubscriptionDto update(Long id, UpdateSnsSubscriptionDto input);
}
