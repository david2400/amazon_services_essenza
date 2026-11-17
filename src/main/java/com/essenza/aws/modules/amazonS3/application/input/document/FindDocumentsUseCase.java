package com.essenza.aws.modules.amazonS3.application.input.document;

import com.essenza.aws.modules.amazonS3.domain.dto.document.DocumentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindDocumentsUseCase {
    Page<DocumentDto> findAll(Pageable pageable);
}
