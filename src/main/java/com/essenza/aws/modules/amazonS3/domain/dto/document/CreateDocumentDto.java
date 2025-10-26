package com.essenza.aws.modules.amazonS3.domain.dto.document;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentDto extends AuditInfoDto {
    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private String newDocument;

    private Integer validityDays;

    private Character isDocumentValidity;
}
