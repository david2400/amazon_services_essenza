package com.essenza.aws.modules.amazonS3.domain.dto.document;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto extends AuditInfoDto {
    private Long id;
    private String name;
    private String description;
    private String newDocument;
    private Integer validityDays;
    private Character isDocumentValidity;
}
