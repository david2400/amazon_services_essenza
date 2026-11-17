package com.essenza.aws.modules.amazonS3.application.services;

import com.essenza.aws.modules.amazonS3.application.input.document_bucket_aws.*;
import com.essenza.aws.modules.amazonS3.application.output.repository.DocumentBucketAwsRepository;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.CreateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.UpdateDocumentBucketAwsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
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

    private final DocumentBucketAwsRepository repository;

    @Override
    public DocumentBucketAwsDto create(CreateDocumentBucketAwsDto input) {
        return repository.create(input);
    }

    @Override
    public DocumentBucketAwsDto update(Integer documentId, Long bucketAwsId, UpdateDocumentBucketAwsDto input) {
        return repository.update(documentId, bucketAwsId, input);
    }

    @Override
    public boolean deleteById(Integer documentId, Long bucketAwsId) {
        return repository.deleteById(documentId, bucketAwsId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DocumentBucketAwsDto> findById(Integer documentId, Long bucketAwsId) {
        return repository.findById(documentId, bucketAwsId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentBucketAwsDto> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentBucketAwsDto> findByDocumentId(Integer documentId, Pageable pageable) {
        return repository.findByDocumentId(documentId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentBucketAwsDto> findByBucketAwsId(Long bucketAwsId, Pageable pageable) {
        return repository.findByBucketAwsId(bucketAwsId, pageable);
    }
}
