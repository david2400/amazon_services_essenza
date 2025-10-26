package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.buketaws;

import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.BuketAwsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaBuketAwsRepository extends JpaRepository<BuketAwsEntity, Long> {
    List<BuketAwsEntity> findByApplicationId(Integer applicationId);
    Optional<BuketAwsEntity> findByProjectName(String projectName);
}
