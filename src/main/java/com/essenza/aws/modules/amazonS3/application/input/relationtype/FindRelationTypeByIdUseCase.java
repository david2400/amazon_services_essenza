package com.essenza.aws.modules.amazonS3.application.input.relationtype;

import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.RelationTypeDto;

import java.util.Optional;

public interface FindRelationTypeByIdUseCase {
    Optional<RelationTypeDto> findById(Integer id);
}
