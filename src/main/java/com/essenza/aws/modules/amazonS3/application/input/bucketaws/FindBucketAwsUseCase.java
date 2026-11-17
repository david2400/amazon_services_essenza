package com.essenza.aws.modules.amazonS3.application.input.bucketaws;

import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.BucketAwsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindBucketAwsUseCase {
    Page<BucketAwsDto> findAll(Pageable pageable);
}
