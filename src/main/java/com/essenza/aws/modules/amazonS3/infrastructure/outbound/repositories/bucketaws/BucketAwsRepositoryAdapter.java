package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.bucketaws;

import com.essenza.aws.modules.amazonS3.application.output.repository.BucketAwsRepository;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.BucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.CreateBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.UpdateBucketAwsDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers.BucketAwsMapper;
import com.essenza.aws.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BucketAwsRepositoryAdapter implements BucketAwsRepository {

    private final JpaBucketAwsRepository jpaRepository;
    private final BucketAwsMapper mapper;

    @Override
    public BucketAwsDto create(CreateBucketAwsDto dto) {
        var entity = mapper.toEntity(dto);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public BucketAwsDto update(Long id, UpdateBucketAwsDto dto) {
        var entity = jpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bucket no encontrado con id " + id));
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
    public Optional<BucketAwsDto> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public Page<BucketAwsDto> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Page<BucketAwsDto> findByApplicationId(Integer applicationId, Pageable pageable) {
        return jpaRepository.findByApplicationId(applicationId, pageable).map(mapper::toDto);
    }

    @Override
    public Optional<BucketAwsDto> findByProjectName(String projectName) {
        return jpaRepository.findByProjectName(projectName).map(mapper::toDto);
    }

    @Override
    public Optional<BucketAwsDto> findByBucketName(String bucketName) {
        return jpaRepository.findByBucketName(bucketName).map(mapper::toDto);
    }
}
