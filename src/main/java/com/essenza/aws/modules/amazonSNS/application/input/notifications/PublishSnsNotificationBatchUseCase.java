package com.essenza.aws.modules.amazonSNS.application.input.notifications;

import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.PublishSnsNotificationDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;

import java.util.List;

public interface PublishSnsNotificationBatchUseCase {
    List<SnsNotificationDto> publishBatch(List<PublishSnsNotificationDto> inputs);
}
