package com.essenza.aws.modules.amazonS3.application.output.repository;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.UpdateFilesAmazonS3Dto;

import java.util.List;

import java.util.Optional;

public interface FilesAmazonS3Repository {

    FilesAmazonS3Dto create(CreateFilesAmazonS3Dto input);

    FilesAmazonS3Dto update(Long id, UpdateFilesAmazonS3Dto input);

    boolean deleteById(Long id);

    Optional<FilesAmazonS3Dto> findById(Long id);

    List<FilesAmazonS3Dto> findAll();

    List<FilesAmazonS3Dto> findByDocumentId(Integer documentId);

    List<FilesAmazonS3Dto> findByRelationTypeId(Integer relationTypeId);

    List<FilesAmazonS3Dto> findByBucketAwsId(Long bucketAwsId);

    List<FilesAmazonS3Dto> findByParentId(Long parentId);
}
