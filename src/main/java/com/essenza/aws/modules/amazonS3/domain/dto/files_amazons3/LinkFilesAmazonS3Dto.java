package com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Metadatos propios de la nueva referencia al vincular un archivo existente a otro
 * bucket/proyecto. Los datos físicos (key, eTag, hash, tamaño) se heredan del objeto.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LinkFilesAmazonS3Dto {

    @Positive
    private Integer documentId;

    @Positive
    private Long parentId;

    private Integer reference;

    private Instant expirationDate;
}
