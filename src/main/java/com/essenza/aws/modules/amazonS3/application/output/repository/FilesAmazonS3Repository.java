package com.essenza.aws.modules.amazonS3.application.output.repository;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.UpdateFilesAmazonS3Dto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FilesAmazonS3Repository {

    FilesAmazonS3Dto create(CreateFilesAmazonS3Dto input);

    FilesAmazonS3Dto update(Long id, UpdateFilesAmazonS3Dto input);

    boolean deleteById(Long id);

    Optional<FilesAmazonS3Dto> findById(Long id);

    /** Puede haber varias referencias lógicas sobre la misma clave (deduplicación). */
    List<FilesAmazonS3Dto> findByKeyAndBucketAwsId(String key, Long bucketAwsId);

    /** Referencias activas que apuntan a un objeto físico (para ref-count al borrar). */
    long countByS3ObjectId(Long s3ObjectId);

    Page<FilesAmazonS3Dto> findAll(Pageable pageable);

    Page<FilesAmazonS3Dto> findByDocumentId(Integer documentId, Pageable pageable);

    Page<FilesAmazonS3Dto> findByBucketAwsId(Long bucketAwsId, Pageable pageable);

    Page<FilesAmazonS3Dto> findByParentId(Long parentId, Pageable pageable);
}
