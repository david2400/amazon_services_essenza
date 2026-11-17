package com.essenza.aws.modules.amazonS3.application.output.repository;

import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.CreateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.UpdateDocumentBucketAwsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * La entidad tiene clave primaria compuesta (document_id, bucket_aws_id):
 * todas las operaciones por id reciben ambos componentes.
 */
public interface DocumentBucketAwsRepository {

    DocumentBucketAwsDto create(CreateDocumentBucketAwsDto input);

    DocumentBucketAwsDto update(Integer documentId, Long bucketAwsId, UpdateDocumentBucketAwsDto input);

    boolean deleteById(Integer documentId, Long bucketAwsId);

    Optional<DocumentBucketAwsDto> findById(Integer documentId, Long bucketAwsId);

    Page<DocumentBucketAwsDto> findAll(Pageable pageable);

    Page<DocumentBucketAwsDto> findByDocumentId(Integer documentId, Pageable pageable);

    Page<DocumentBucketAwsDto> findByBucketAwsId(Long bucketAwsId, Pageable pageable);
}
