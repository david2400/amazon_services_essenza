package com.essenza.aws.modules.amazonS3.application.input.files_amazon;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindFilesAmazonS3ByParentIdUseCase {
    Page<FilesAmazonS3Dto> findByParentId(Long parentId, Pageable pageable);
}
