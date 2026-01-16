package com.essenza.aws.modules.amazonS3.application.input.relation_type;

import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.RelationTypeDto;
import com.essenza.aws.modules.amazonS3.domain.dto.relation_type.UpdateRelationTypeDto;

public interface UpdateRelationTypeUseCase {
    RelationTypeDto update(Integer id, UpdateRelationTypeDto input);
}
