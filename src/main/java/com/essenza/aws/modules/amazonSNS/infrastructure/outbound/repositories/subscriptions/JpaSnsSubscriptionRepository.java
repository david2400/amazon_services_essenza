package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.subscriptions;

import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.persistence.mysql.sns.SnsSubscriptionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaSnsSubscriptionRepository extends JpaRepository<SnsSubscriptionEntity, Long> {
    Optional<SnsSubscriptionEntity> findBySubscriptionArn(String subscriptionArn);

    Page<SnsSubscriptionEntity> findByTopicArn(String topicArn, Pageable pageable);
}
