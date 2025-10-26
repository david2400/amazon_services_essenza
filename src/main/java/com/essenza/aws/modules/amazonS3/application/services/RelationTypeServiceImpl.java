package com.essenza.aws.modules.amazonS3.application.services;

import com.essenza.aws.modules.amazonS3.application.input.relationtype.CreateRelationTypeUseCase;
import com.essenza.aws.modules.amazonS3.application.input.relationtype.DeleteRelationTypeUseCase;
import com.essenza.aws.modules.amazonS3.application.input.relationtype.FindRelationTypeByIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.relationtype.FindRelationTypeByNameUseCase;
import com.essenza.aws.modules.amazonS3.application.input.relationtype.FindRelationTypesUseCase;
import com.essenza.aws.modules.amazonS3.application.input.relationtype.UpdateRelationTypeUseCase;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.CreateRelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.RelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.UpdateRelationTypeDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.relationtype.RelationTypeRepositoryAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RelationTypeServiceImpl implements
        CreateRelationTypeUseCase,
        UpdateRelationTypeUseCase,
        DeleteRelationTypeUseCase,
        FindRelationTypeByIdUseCase,
        FindRelationTypesUseCase,
        FindRelationTypeByNameUseCase {

    private final RelationTypeRepositoryAdapter repository;

    public RelationTypeServiceImpl(RelationTypeRepositoryAdapter repository) {
        this.repository = repository;
    }

    @Override
    public RelationTypeDto create(CreateRelationTypeDto input) {
        return repository.create(input);
    }

    @Override
    public RelationTypeDto update(Integer id, UpdateRelationTypeDto input) {
        return repository.update(id, input);
    }

    @Override
    public boolean deleteById(Integer id) {
        return repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RelationTypeDto> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelationTypeDto> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RelationTypeDto> findByName(String name) {
        return repository.findByName(name);
    }
}
