package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.notifications;

import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.persistence.mysql.sns.SnsNotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSnsNotificationRepository extends JpaRepository<SnsNotificationEntity, Long> {
    Page<SnsNotificationEntity> findByTopicArn(String topicArn, Pageable pageable);
}
