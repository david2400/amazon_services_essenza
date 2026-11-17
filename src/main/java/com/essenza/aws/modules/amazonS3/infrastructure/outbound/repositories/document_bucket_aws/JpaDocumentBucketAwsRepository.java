package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.document_bucket_aws;

import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.DocumentBucketAws.DocumentBucketAwsEntity;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.DocumentBucketAws.DocumentBucketAwsId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDocumentBucketAwsRepository extends JpaRepository<DocumentBucketAwsEntity, DocumentBucketAwsId> {
    Page<DocumentBucketAwsEntity> findByDocumentId(Integer documentId, Pageable pageable);

    Page<DocumentBucketAwsEntity> findByBucketAwsId(Long bucketAwsId, Pageable pageable);
}
