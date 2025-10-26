package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.relationtype;

import com.essenza.aws.modules.amazonS3.application.output.repository.RelationTypeRepository;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.CreateRelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.RelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.UpdateRelationTypeDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers.RelationTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RelationTypeRepositoryAdapter implements RelationTypeRepository {

    private final JpaRelationTypeRepository jpaRepository;
    private final RelationTypeMapper mapper;

    @Override
    public RelationTypeDto create(CreateRelationTypeDto input) {
        var entity = mapper.toEntity(input);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public RelationTypeDto update(Integer id, UpdateRelationTypeDto dto) {
        var entity = jpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RelationType not found"));

        mapper.updateEntityFromDto(dto, entity);
        var updated = jpaRepository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    public boolean deleteById(Integer id) {
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<RelationTypeDto> findById(Integer id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public List<RelationTypeDto> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public Optional<RelationTypeDto> findByName(String name) {
        return jpaRepository.findByName(name).map(mapper::toDto);
    }
}
