package com.essenza.aws.modules.amazonS3.application.input.filesAmazon;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import org.springframework.web.multipart.MultipartFile;

public interface UploadFileUseCase {
    FilesAmazonS3Dto upload(MultipartFile file,
                            String bucket,
                            String folder,
                            CreateFilesAmazonS3Dto meta,
                            String userId);
}
