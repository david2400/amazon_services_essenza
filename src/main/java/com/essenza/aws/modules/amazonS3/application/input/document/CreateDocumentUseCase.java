package com.essenza.aws.modules.amazonS3.application.input.document;

import com.essenza.aws.modules.amazonS3.domain.dto.document.CreateDocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.DocumentDto;

public interface CreateDocumentUseCase {
    DocumentDto create(CreateDocumentDto input);
}
