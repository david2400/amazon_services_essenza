package com.essenza.aws.modules.amazonSQS.infrastructure.outbound.repositories.queues;

import com.essenza.aws.modules.amazonSQS.infrastructure.outbound.persistence.mysql.sqs.SqsQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaSqsQueueRepository extends JpaRepository<SqsQueueEntity, Long> {
    Optional<SqsQueueEntity> findByQueueUrl(String queueUrl);
}
