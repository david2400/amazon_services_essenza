package com.essenza.aws.modules.amazonS3.application.input.document_bucket_aws;

import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;

import java.util.List;

public interface FindDocumentBucketAwsUseCase {
    List<DocumentBucketAwsDto> findAll();
}
