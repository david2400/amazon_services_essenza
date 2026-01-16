package com.essenza.aws.modules.amazonS3.application.input.relation_type;

import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.CreateRelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.RelationTypeDto;

public interface CreateRelationTypeUseCase {
    RelationTypeDto create(CreateRelationTypeDto input);
}
