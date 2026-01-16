package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.document_bucket_aws;

import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.DocumentBucketAwsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaDocumentBucketAwsRepository extends JpaRepository<DocumentBucketAwsEntity, Long> {
    List<DocumentBucketAwsEntity> findByDocumentId(Integer documentId);
    List<DocumentBucketAwsEntity> findByBucketAwsId(Long bucketAwsId);
}
