package com.essenza.aws.modules.amazonS3.application.input.bucketaws;

import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.BucketAwsDto;

import java.util.Optional;

public interface FindBucketAwsByIdUseCase {
    Optional<BucketAwsDto> findById(Long id);
}
