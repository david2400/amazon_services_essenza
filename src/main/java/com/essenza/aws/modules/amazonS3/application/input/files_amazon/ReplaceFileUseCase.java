package com.essenza.aws.modules.amazonS3.application.input.files_amazon;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import org.springframework.web.multipart.MultipartFile;

public interface ReplaceFileUseCase {
    FilesAmazonS3Dto replace(Long id, MultipartFile file);
}
