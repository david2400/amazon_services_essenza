package com.essenza.aws.modules.amazonS3.application.input.relation_type;

import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.RelationTypeDto;

import java.util.Optional;

public interface FindRelationTypeByNameUseCase {
    Optional<RelationTypeDto> findByName(String name);
}
