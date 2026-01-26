package com.essenza.aws.modules.amazonSNS.application.input.subscriptions;

import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.CreateSnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;

public interface CreateSnsSubscriptionUseCase {
    SnsSubscriptionDto subscribe(CreateSnsSubscriptionDto input);
}
