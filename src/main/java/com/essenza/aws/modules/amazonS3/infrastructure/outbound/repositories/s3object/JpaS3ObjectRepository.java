package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.s3object;

import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.S3ObjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaS3ObjectRepository extends JpaRepository<S3ObjectEntity, Long> {

    Optional<S3ObjectEntity> findByBucketAwsIdAndContentHash(Long bucketAwsId, String contentHash);

    Page<S3ObjectEntity> findByBucketAwsId(Long bucketAwsId, Pageable pageable);
}
