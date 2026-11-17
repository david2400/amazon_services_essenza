package com.essenza.aws.modules.amazonS3.domain.dto.s3object;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Objeto físico en S3 (una sola copia por contenido dentro de un bucket). */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class S3ObjectDto extends AuditInfoDto {
    private Long id;
    private Long bucketAwsId;
    private String objectKey;
    private String contentHash;
    private Long sizeBytes;
    private String contentType;
    private String eTag;
    private String s3Url;
}
