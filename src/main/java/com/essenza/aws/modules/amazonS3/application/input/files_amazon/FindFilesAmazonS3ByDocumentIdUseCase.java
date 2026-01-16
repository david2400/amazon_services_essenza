package com.essenza.aws.modules.amazonS3.application.input.files_amazon;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;

import java.util.List;

public interface FindFilesAmazonS3ByDocumentIdUseCase {
    List<FilesAmazonS3Dto> findByDocumentId(Integer documentId);
}
