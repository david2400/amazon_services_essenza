package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.notifications;

import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.persistence.mysql.sns.SnsNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSnsNotificationRepository extends JpaRepository<SnsNotificationEntity, Long> {
    List<SnsNotificationEntity> findByTopicArn(String topicArn);
}
