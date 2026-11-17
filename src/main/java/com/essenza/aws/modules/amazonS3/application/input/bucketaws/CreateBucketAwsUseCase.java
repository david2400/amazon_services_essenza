package com.essenza.aws.modules.amazonS3.application.input.bucketaws;

import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.BucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.CreateBucketAwsDto;

public interface CreateBucketAwsUseCase {
    BucketAwsDto create(CreateBucketAwsDto input);
}
