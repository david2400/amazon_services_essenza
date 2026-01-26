package com.essenza.aws.modules.amazonSNS.application.input.subscriptions;

import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;

import java.util.List;

public interface FindSnsSubscriptionsByTopicUseCase {
    List<SnsSubscriptionDto> findByTopicArn(String topicArn);
}
