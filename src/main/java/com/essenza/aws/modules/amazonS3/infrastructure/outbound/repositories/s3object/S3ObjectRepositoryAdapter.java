package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.s3object;

import com.essenza.aws.modules.amazonS3.application.output.repository.S3ObjectRepository;
import com.essenza.aws.modules.amazonS3.domain.dto.s3object.S3ObjectDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers.S3ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class S3ObjectRepositoryAdapter implements S3ObjectRepository {

    private final JpaS3ObjectRepository jpaRepository;
    private final S3ObjectMapper mapper;

    @Override
    public S3ObjectDto save(S3ObjectDto input) {
        var entity = mapper.toEntity(input);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public Optional<S3ObjectDto> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<S3ObjectDto> findByBucketAwsIdAndContentHash(Long bucketAwsId, String contentHash) {
        return jpaRepository.findByBucketAwsIdAndContentHash(bucketAwsId, contentHash).map(mapper::toDto);
    }

    @Override
    public Page<S3ObjectDto> findByBucketAwsId(Long bucketAwsId, Pageable pageable) {
        return jpaRepository.findByBucketAwsId(bucketAwsId, pageable).map(mapper::toDto);
    }

    @Override
    public boolean deleteById(Long id) {
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
