package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.bucketaws;

import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.BucketAwsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaBucketAwsRepository extends JpaRepository<BucketAwsEntity, Long> {
    Page<BucketAwsEntity> findByApplicationId(Integer applicationId, Pageable pageable);

    Optional<BucketAwsEntity> findByProjectName(String projectName);

    Optional<BucketAwsEntity> findByBucketName(String bucketName);
}
