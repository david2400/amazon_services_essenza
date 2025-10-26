package com.essenza.aws.modules.amazonS3.application.input.document;

import com.essenza.aws.modules.amazonS3.domain.dto.document.DocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.UpdateDocumentDto;

public interface UpdateDocumentUseCase {
    DocumentDto update(Integer id, UpdateDocumentDto input);
}
