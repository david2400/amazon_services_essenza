package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.repositories.topics;

import com.essenza.aws.modules.amazonSNS.infrastructure.outbound.persistence.mysql.sns.SnsTopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaSnsTopicRepository extends JpaRepository<SnsTopicEntity, Long> {
    Optional<SnsTopicEntity> findByArn(String arn);
}
