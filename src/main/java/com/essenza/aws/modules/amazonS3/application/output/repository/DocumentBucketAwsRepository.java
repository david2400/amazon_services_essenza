package com.essenza.aws.modules.amazonS3.application.output.repository;

import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.CreateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.UpdateDocumentBucketAwsDto;

import java.util.List;
import java.util.Optional;

public interface DocumentBucketAwsRepository {

    DocumentBucketAwsDto create(CreateDocumentBucketAwsDto input);

    DocumentBucketAwsDto update(Long id, UpdateDocumentBucketAwsDto input);

    boolean deleteById(Long id);

    Optional<DocumentBucketAwsDto> findById(Long id);

    List<DocumentBucketAwsDto> findAll();

    List<DocumentBucketAwsDto> findByDocumentId(Integer documentId);

    List<DocumentBucketAwsDto> findByBucketAwsId(Long bucketAwsId);
}
