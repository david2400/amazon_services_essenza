package com.essenza.aws.modules.amazonS3.application.input.files_amazon;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;

public interface UpdateDocumentIdUseCase {
    FilesAmazonS3Dto updateDocumentId(Long id, Integer documentId, String userId);
}
