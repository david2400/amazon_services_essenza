package com.essenza.aws.modules.amazonS3.application.input.document_bucket_aws;

import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.CreateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;

public interface CreateDocumentBucketAwsUseCase {
    DocumentBucketAwsDto create(CreateDocumentBucketAwsDto input);
}
