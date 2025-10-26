package com.essenza.aws.modules.amazonS3.application.input.document;

import com.essenza.aws.modules.amazonS3.domain.dto.document.DocumentDto;

import java.util.List;

public interface FindDocumentsUseCase {
    List<DocumentDto> findAll();
}
