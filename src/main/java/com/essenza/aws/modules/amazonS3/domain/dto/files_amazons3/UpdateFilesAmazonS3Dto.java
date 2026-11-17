package com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Actualización parcial: todos los campos son opcionales; los nulos se ignoran
 * (ver {@code NullValuePropertyMappingStrategy.IGNORE} en el mapper).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFilesAmazonS3Dto {
    private Long parentId;

    private String fileName;

    private String key;

    @Positive
    private Integer documentId;

    private Instant expirationDate;

    private String eTag;

    private String route;

    private String s3Url;

    private Integer reference;

    @Positive
    private Long bucketAwsId;

    /** Uso interno: repunta la referencia a otro objeto físico (replace/dedup). */
    private Long s3ObjectId;

    private String contentHash;

    private Long sizeBytes;
}
