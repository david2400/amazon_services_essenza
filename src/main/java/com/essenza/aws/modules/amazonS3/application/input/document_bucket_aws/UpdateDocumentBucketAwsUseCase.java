package com.essenza.aws.modules.amazonS3.application.input.document_bucket_aws;

import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.UpdateDocumentBucketAwsDto;

public interface UpdateDocumentBucketAwsUseCase {
    DocumentBucketAwsDto update(Long id, UpdateDocumentBucketAwsDto input);
}
