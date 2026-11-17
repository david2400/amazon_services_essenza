package com.essenza.aws.modules.amazonS3.application.input.files_amazon;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;

public interface DeleteFileByKeyAndBucketUseCase {
    FilesAmazonS3Dto deleteByKeyAndBucket(String key, Long bucketAwsId);
}
