package com.essenza.aws.modules.amazonS3.application.output.repository;

import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.BucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.CreateBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.UpdateBucketAwsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BucketAwsRepository {

    BucketAwsDto create(CreateBucketAwsDto input);

    BucketAwsDto update(Long id, UpdateBucketAwsDto input);

    boolean deleteById(Long id);

    Optional<BucketAwsDto> findById(Long id);

    Page<BucketAwsDto> findAll(Pageable pageable);

    Page<BucketAwsDto> findByApplicationId(Integer applicationId, Pageable pageable);

    Optional<BucketAwsDto> findByProjectName(String projectName);

    Optional<BucketAwsDto> findByBucketName(String bucketName);
}
