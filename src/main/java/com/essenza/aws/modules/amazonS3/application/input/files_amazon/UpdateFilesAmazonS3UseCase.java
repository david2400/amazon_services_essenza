package com.essenza.aws.modules.amazonS3.application.input.files_amazon;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.UpdateFilesAmazonS3Dto;

public interface UpdateFilesAmazonS3UseCase {
    FilesAmazonS3Dto update(Long id, UpdateFilesAmazonS3Dto input);
}
