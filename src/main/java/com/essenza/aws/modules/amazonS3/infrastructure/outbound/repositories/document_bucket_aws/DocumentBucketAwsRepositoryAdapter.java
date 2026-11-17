package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.document_bucket_aws;

import com.essenza.aws.modules.amazonS3.application.output.repository.DocumentBucketAwsRepository;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.CreateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.UpdateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers.DocumentBucketAwsMapper;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.DocumentBucketAws.DocumentBucketAwsId;
import com.essenza.aws.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentBucketAwsRepositoryAdapter implements DocumentBucketAwsRepository {

    private final JpaDocumentBucketAwsRepository jpaRepository;
    private final DocumentBucketAwsMapper mapper;

    @Override
    public DocumentBucketAwsDto create(CreateDocumentBucketAwsDto input) {
        var entity = mapper.toEntity(input);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public DocumentBucketAwsDto update(Integer documentId, Long bucketAwsId, UpdateDocumentBucketAwsDto input) {
        var entity = jpaRepository.findById(compositeId(documentId, bucketAwsId))
                .orElseThrow(() -> new NotFoundException(
                        "Relación documento-bucket no encontrada (documentId " + documentId + ", bucketAwsId " + bucketAwsId + ")"));
        mapper.updateEntityFromDto(input, entity);
        var updated = jpaRepository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    public boolean deleteById(Integer documentId, Long bucketAwsId) {
        var id = compositeId(documentId, bucketAwsId);
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<DocumentBucketAwsDto> findById(Integer documentId, Long bucketAwsId) {
        return jpaRepository.findById(compositeId(documentId, bucketAwsId)).map(mapper::toDto);
    }

    @Override
    public Page<DocumentBucketAwsDto> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Page<DocumentBucketAwsDto> findByDocumentId(Integer documentId, Pageable pageable) {
        return jpaRepository.findByDocumentId(documentId, pageable).map(mapper::toDto);
    }

    @Override
    public Page<DocumentBucketAwsDto> findByBucketAwsId(Long bucketAwsId, Pageable pageable) {
        return jpaRepository.findByBucketAwsId(bucketAwsId, pageable).map(mapper::toDto);
    }

    private DocumentBucketAwsId compositeId(Integer documentId, Long bucketAwsId) {
        return new DocumentBucketAwsId(documentId, bucketAwsId);
    }
}
