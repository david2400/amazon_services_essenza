package com.essenza.aws.modules.amazonSNS.application.output.repository;

import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;

import java.util.List;

public interface SnsNotificationRepository {
    SnsNotificationDto save(SnsNotificationDto input);

    List<SnsNotificationDto> findByTopicArn(String topicArn);

    List<SnsNotificationDto> findAll();
}
