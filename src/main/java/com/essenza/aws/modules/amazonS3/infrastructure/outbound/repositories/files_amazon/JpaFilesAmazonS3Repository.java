package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.files_amazon;

import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.FilesAmazonS3Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaFilesAmazonS3Repository extends JpaRepository<FilesAmazonS3Entity, Long> {
    List<FilesAmazonS3Entity> findByDocumentId(Integer documentId);
    List<FilesAmazonS3Entity> findByRelationTypeId(Integer relationTypeId);
    List<FilesAmazonS3Entity> findByBucketAwsId(Long bucketAwsId);
    List<FilesAmazonS3Entity> findByParentId(Long parentId);
    List<FilesAmazonS3Entity> findByKeyAndBucketAwsId(String key, Long bucketAwsId);
}
