package com.essenza.aws.modules.amazonS3.application.input.document;

import com.essenza.aws.modules.amazonS3.domain.dto.document.DocumentDto;

import java.util.Optional;

public interface FindDocumentByNameUseCase {
    Optional<DocumentDto> findByName(String name);
}
