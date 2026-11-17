package com.essenza.aws.modules.amazonS3.domain.dto.bucketaws;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBucketAwsDto extends AuditInfoDto {
    private String projectName;

    @NotNull
    @Positive
    private Integer applicationId;

    @NotBlank
    @Size(max = 63)
    private String bucketName;

    @Size(max = 255)
    private String bucketFolder;
}
