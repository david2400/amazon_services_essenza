package com.essenza.aws.modules.amazonS3.domain.dto.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Actualización parcial: todos los campos son opcionales; los nulos se ignoran.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDocumentDto {
    private String name;

    private String description;

    private String newDocument;

    private Integer validityDays;

    private Character isDocumentValidity;
}
