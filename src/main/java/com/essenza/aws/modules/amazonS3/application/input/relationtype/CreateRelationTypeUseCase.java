package com.essenza.aws.modules.amazonS3.application.input.relationtype;

import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.CreateRelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.RelationTypeDto;

public interface CreateRelationTypeUseCase {
    RelationTypeDto create(CreateRelationTypeDto input);
}
