package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.buketaws;

import com.essenza.aws.modules.amazonS3.application.output.repository.BuketAwsRepository;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.BuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.CreateBuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.UpdateBuketAwsDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers.BuketAwsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BuketAwsRepositoryAdapter implements BuketAwsRepository {

    private final JpaBuketAwsRepository jpaRepository;
    private final BuketAwsMapper mapper;

    @Override
    public BuketAwsDto create(CreateBuketAwsDto dto) {
        var entity = mapper.toEntity(dto);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public BuketAwsDto update(Long id, UpdateBuketAwsDto dto) {
        var entity = jpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BucketAws not found"));
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
    public Optional<BuketAwsDto> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public List<BuketAwsDto> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BuketAwsDto> findByApplicationId(Integer applicationId) {
        return jpaRepository.findByApplicationId(applicationId).stream().map(mapper::toDto).toList();
    }

    @Override
    public Optional<BuketAwsDto> findByProjectName(String projectName) {
        return jpaRepository.findByProjectName(projectName).map(mapper::toDto);
    }
}
