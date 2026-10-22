package com.essenza.aws.modules.amazonSNS.application.input.subscriptions;

import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindSnsSubscriptionsByTopicUseCase {
    Page<SnsSubscriptionDto> findByTopicArn(String topicArn, Pageable pageable);
}
