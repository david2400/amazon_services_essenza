package com.essenza.aws.modules.amazonS3.application.services;

import com.essenza.aws.modules.amazonS3.application.input.document_bucket_aws.*;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.CreateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.UpdateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.document_bucket_aws.DocumentBucketAwsRepositoryAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DocumentBucketAwsServiceImpl implements
        CreateDocumentBucketAwsUseCase,
        UpdateDocumentBucketAwsUseCase,
        DeleteDocumentBucketAwsUseCase,
        FindDocumentBucketAwsByIdUseCase,
        FindDocumentBucketAwsUseCase,
        FindDocumentBucketAwsByDocumentIdUseCase,
        FindDocumentBucketAwsByBucketAwsIdUseCase {

    private final DocumentBucketAwsRepositoryAdapter repository;

    public DocumentBucketAwsServiceImpl(DocumentBucketAwsRepositoryAdapter repository) {
        this.repository = repository;
    }

    @Override
    public DocumentBucketAwsDto create(CreateDocumentBucketAwsDto input) {
        return repository.create(input);
    }

    @Override
    public DocumentBucketAwsDto update(Long id, UpdateDocumentBucketAwsDto input) {
        return repository.update(id, input);
    }

    @Override
    public boolean deleteById(Long id) {
        return repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DocumentBucketAwsDto> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentBucketAwsDto> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentBucketAwsDto> findByDocumentId(Integer documentId) {
        return repository.findByDocumentId(documentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentBucketAwsDto> findByBucketAwsId(Long bucketAwsId) {
        return repository.findByBucketAwsId(bucketAwsId);
    }
}
