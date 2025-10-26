package com.essenza.aws.modules.amazonS3.application.input.filesAmazon;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;

import java.util.List;

public interface FindFilesAmazonS3ByBucketAwsIdUseCase {
    List<FilesAmazonS3Dto> findByBucketAwsId(Long bucketAwsId);
}
