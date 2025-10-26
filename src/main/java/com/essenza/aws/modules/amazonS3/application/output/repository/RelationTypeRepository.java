package com.essenza.aws.modules.amazonS3.application.output.repository;

import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.CreateRelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.RelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.UpdateRelationTypeDto;

import java.util.List;
import java.util.Optional;

public interface RelationTypeRepository {

    RelationTypeDto create(CreateRelationTypeDto input);

    RelationTypeDto update(Integer id, UpdateRelationTypeDto input);

    boolean deleteById(Integer id);

    Optional<RelationTypeDto> findById(Integer id);

    List<RelationTypeDto> findAll();

    Optional<RelationTypeDto> findByName(String name);
}
