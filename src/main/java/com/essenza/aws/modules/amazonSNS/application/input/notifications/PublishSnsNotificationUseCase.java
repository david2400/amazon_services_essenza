package com.essenza.aws.modules.amazonSNS.application.input.notifications;

import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.PublishSnsNotificationDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;

public interface PublishSnsNotificationUseCase {
    SnsNotificationDto publish(PublishSnsNotificationDto input);
}
