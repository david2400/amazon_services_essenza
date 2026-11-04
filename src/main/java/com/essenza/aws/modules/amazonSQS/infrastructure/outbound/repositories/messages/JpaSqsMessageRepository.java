package com.essenza.aws.modules.amazonSQS.infrastructure.outbound.repositories.messages;

import com.essenza.aws.modules.amazonSQS.infrastructure.outbound.persistence.mysql.sqs.SqsMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSqsMessageRepository extends JpaRepository<SqsMessageEntity, Long> {
    Page<SqsMessageEntity> findByQueueUrl(String queueUrl, Pageable pageable);
}
