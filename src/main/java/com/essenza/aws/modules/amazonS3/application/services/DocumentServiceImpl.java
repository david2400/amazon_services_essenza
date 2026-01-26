package com.essenza.aws.modules.amazonS3.application.services;

import com.essenza.aws.modules.amazonS3.application.input.document.CreateDocumentUseCase;
import com.essenza.aws.modules.amazonS3.application.input.document.DeleteDocumentUseCase;
import com.essenza.aws.modules.amazonS3.application.input.document.FindDocumentByIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.document.FindDocumentByNameUseCase;
import com.essenza.aws.modules.amazonS3.application.input.document.FindDocumentsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.document.UpdateDocumentUseCase;
import com.essenza.aws.modules.amazonS3.domain.dto.document.CreateDocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.DocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.UpdateDocumentDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.document.DocumentRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class DocumentServiceImpl implements
        CreateDocumentUseCase,
        UpdateDocumentUseCase,
        DeleteDocumentUseCase,
        FindDocumentByIdUseCase,
        FindDocumentsUseCase,
        FindDocumentByNameUseCase {

    private final DocumentRepositoryAdapter repository;

//    public DocumentServiceImpl(DocumentRepositoryAdapter repository) {
//        this.repository = repository;
//    }

    @Override
    public DocumentDto create(CreateDocumentDto input) {
        return repository.create(input);
    }

    @Override
    public DocumentDto update(Integer id, UpdateDocumentDto input) {
        return repository.update(id, input);
    }

    @Override
    public boolean deleteById(Integer id) {
        return repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DocumentDto> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDto> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DocumentDto> findByName(String name) {
        return repository.findByName(name);
    }
}
