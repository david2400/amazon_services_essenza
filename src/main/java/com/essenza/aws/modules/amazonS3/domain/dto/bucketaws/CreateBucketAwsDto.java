package com.essenza.aws.modules.amazonS3.domain.dto.buketaws;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBuketAwsDto extends AuditInfoDto {
    private String projectName;

    @NotNull
    @Positive
    private Integer applicationId;

    private String bucketName;

    private String bucketFolder;
}
