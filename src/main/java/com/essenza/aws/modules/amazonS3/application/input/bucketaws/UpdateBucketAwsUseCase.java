package com.essenza.aws.modules.amazonS3.application.input.bucketaws;

import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.BucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.UpdateBucketAwsDto;

public interface UpdateBucketAwsUseCase {
    BucketAwsDto update(Long id, UpdateBucketAwsDto input);
}
