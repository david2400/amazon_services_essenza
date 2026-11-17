package com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFilesAmazonS3Dto extends AuditInfoDto {
    private Long parentId;

    private String fileName;

    /** Se genera en el servicio (folder + UUID); no es obligatorio en la entrada. */
    private String key;

    @Positive
    private Integer documentId;

    private Instant expirationDate;

    private String eTag;

    private String route;

    private String s3Url;

    private Integer reference;

    @NotNull
    @Positive
    private Long bucketAwsId;

    /** Uso interno: referencia al objeto físico en s3_objects (deduplicación). */
    private Long s3ObjectId;

    /** Uso interno: SHA-256 del contenido y tamaño en bytes. */
    private String contentHash;

    private Long sizeBytes;
}
