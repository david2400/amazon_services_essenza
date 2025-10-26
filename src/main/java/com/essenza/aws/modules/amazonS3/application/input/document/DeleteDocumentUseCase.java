package com.essenza.aws.modules.amazonS3.application.input.document;

public interface DeleteDocumentUseCase {
    boolean deleteById(Integer id);
}
