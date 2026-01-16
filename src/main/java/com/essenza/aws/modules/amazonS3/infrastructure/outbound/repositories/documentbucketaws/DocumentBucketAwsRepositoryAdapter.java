package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.document_bucket_aws;

import com.essenza.aws.modules.amazonS3.application.output.repository.DocumentBucketAwsRepository;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.CreateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.UpdateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers.DocumentBucketAwsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public DocumentBucketAwsDto update(Long id, UpdateDocumentBucketAwsDto input) {
        var entity = jpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DocumentBucketAws not found"));
        mapper.updateEntityFromDto(input, entity);
        var updated = jpaRepository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    public boolean deleteById(Long id) {
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<DocumentBucketAwsDto> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public List<DocumentBucketAwsDto> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public List<DocumentBucketAwsDto> findByDocumentId(Integer documentId) {
        return jpaRepository.findByDocumentId(documentId).stream().map(mapper::toDto).toList();
    }

    @Override
    public List<DocumentBucketAwsDto> findByBucketAwsId(Long bucketAwsId) {
        return jpaRepository.findByBucketAwsId(bucketAwsId).stream().map(mapper::toDto).toList();
    }
}
