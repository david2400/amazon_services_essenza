package com.essenza.aws.modules.amazonS3.application.input.filesAmazon;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;

import java.util.Optional;

public interface FindFilesAmazonS3ByIdUseCase {
    Optional<FilesAmazonS3Dto> findById(Long id);
}
