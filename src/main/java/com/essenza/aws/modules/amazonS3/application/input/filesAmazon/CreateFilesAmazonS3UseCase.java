package com.essenza.aws.modules.amazonS3.application.input.filesAmazon;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;

public interface CreateFilesAmazonS3UseCase {
    FilesAmazonS3Dto create(CreateFilesAmazonS3Dto input);
}
