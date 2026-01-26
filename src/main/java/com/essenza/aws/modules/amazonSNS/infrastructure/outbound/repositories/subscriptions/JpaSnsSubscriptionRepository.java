package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.subscriptions;

import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.persistence.mysql.sns.SnsSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaSnsSubscriptionRepository extends JpaRepository<SnsSubscriptionEntity, Long> {
    Optional<SnsSubscriptionEntity> findBySubscriptionArn(String subscriptionArn);

    List<SnsSubscriptionEntity> findByTopicArn(String topicArn);
}
