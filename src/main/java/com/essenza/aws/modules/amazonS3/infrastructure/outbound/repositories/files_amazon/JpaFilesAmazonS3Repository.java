package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.files_amazon;

import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.FilesAmazonS3Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaFilesAmazonS3Repository extends JpaRepository<FilesAmazonS3Entity, Long> {
    Page<FilesAmazonS3Entity> findByDocumentId(Integer documentId, Pageable pageable);

    Page<FilesAmazonS3Entity> findByBucketAwsId(Long bucketAwsId, Pageable pageable);

    Page<FilesAmazonS3Entity> findByParentId(Long parentId, Pageable pageable);

    List<FilesAmazonS3Entity> findByKeyAndBucketAwsId(String key, Long bucketAwsId);

    long countByS3ObjectId(Long s3ObjectId);
}
