package com.essenza.aws.modules.amazonSNS.application.input.notifications;

import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;

import java.util.List;

public interface FindSnsNotificationsByTopicUseCase {
    List<SnsNotificationDto> findByTopicArn(String topicArn);
}
