package com.essenza.aws.modules.amazonS3.domain.dto.relation_type;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RelationTypeDto extends AuditInfoDto {
    private Long id;
    private String name;
    private Integer relationTypeId;
}
