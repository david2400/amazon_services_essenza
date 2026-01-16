package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.files_amazon;

import com.essenza.aws.modules.amazonS3.application.output.repository.FilesAmazonS3Repository;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.UpdateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers.FilesAmazonS3Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FilesAmazonS3RepositoryAdapter implements FilesAmazonS3Repository {

    private final JpaFilesAmazonS3Repository jpaRepository;
    private final FilesAmazonS3Mapper mapper;

    @Override
    public FilesAmazonS3Dto create(CreateFilesAmazonS3Dto input) {
        var entity = mapper.toEntity(input);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public FilesAmazonS3Dto update(Long id, UpdateFilesAmazonS3Dto dto) {
        var entity = jpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FilesAmazonS3 not found"));

        mapper.updateEntityFromDto(dto, entity);
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
    public Optional<FilesAmazonS3Dto> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public List<FilesAmazonS3Dto> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public List<FilesAmazonS3Dto> findByDocumentId(Integer documentId) {
        return jpaRepository.findByDocumentId(documentId).stream().map(mapper::toDto).toList();
    }

    @Override
    public List<FilesAmazonS3Dto> findByRelationTypeId(Integer relationTypeId) {
        return jpaRepository.findByRelationTypeId(relationTypeId).stream().map(mapper::toDto).toList();
    }

    @Override
    public List<FilesAmazonS3Dto> findByBucketAwsId(Long bucketAwsId) {
        return jpaRepository.findByBucketAwsId(bucketAwsId).stream().map(mapper::toDto).toList();
    }

    @Override
    public List<FilesAmazonS3Dto> findByParentId(Long parentId) {
        return jpaRepository.findByParentId(parentId).stream().map(mapper::toDto).toList();
    }
}
