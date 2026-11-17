package com.essenza.aws.modules.amazonS3.application.input.document_bucket_aws;

public interface DeleteDocumentBucketAwsUseCase {
    boolean deleteById(Integer documentId, Long bucketAwsId);
}
