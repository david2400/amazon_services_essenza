package com.essenza.aws.modules.amazonSNS.application.input.notifications;

import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindSnsNotificationsUseCase {
    Page<SnsNotificationDto> findAll(Pageable pageable);
}
