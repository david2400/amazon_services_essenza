package com.essenza.aws.modules.amazonS3.application.output.repository;

import com.essenza.aws.modules.amazonS3.domain.dto.document.CreateDocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.DocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.UpdateDocumentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DocumentRepository {

    DocumentDto create(CreateDocumentDto input);

    DocumentDto update(Integer id, UpdateDocumentDto input);

    boolean deleteById(Integer id);

    Optional<DocumentDto> findById(Integer id);

    Page<DocumentDto> findAll(Pageable pageable);

    Optional<DocumentDto> findByName(String name);
}
