package com.essenza.aws.modules.amazonS3.application.output.repository;

import com.essenza.aws.modules.amazonS3.domain.dto.document.CreateDocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.DocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.UpdateDocumentDto;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository {

    DocumentDto create(CreateDocumentDto input);

    DocumentDto update(Integer id, UpdateDocumentDto input);

    boolean deleteById(Integer id);

    Optional<DocumentDto> findById(Integer id);

    List<DocumentDto> findAll();

    Optional<DocumentDto> findByName(String name);
}
