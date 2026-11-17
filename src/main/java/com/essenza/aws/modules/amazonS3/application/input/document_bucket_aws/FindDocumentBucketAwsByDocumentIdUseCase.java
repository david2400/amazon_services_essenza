package com.essenza.aws.modules.amazonS3.application.input.document_bucket_aws;

import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindDocumentBucketAwsByDocumentIdUseCase {
    Page<DocumentBucketAwsDto> findByDocumentId(Integer documentId, Pageable pageable);
}
