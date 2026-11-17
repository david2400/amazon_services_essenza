package com.essenza.aws.modules.amazonS3.domain.dto.bucketaws;

import jakarta.validation.constraints.Positive;
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
public class UpdateBucketAwsDto {
    private String projectName;

    @Positive
    private Integer applicationId;

    private String bucketName;

    private String bucketFolder;
}
