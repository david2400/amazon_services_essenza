package com.essenza.aws.modules.amazonSNS.application.output.repository;

import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SnsNotificationRepository {
    SnsNotificationDto save(SnsNotificationDto input);

    Page<SnsNotificationDto> findByTopicArn(String topicArn, Pageable pageable);

    Page<SnsNotificationDto> findAll(Pageable pageable);
}
