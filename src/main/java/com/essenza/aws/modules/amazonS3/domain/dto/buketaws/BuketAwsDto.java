package com.essenza.aws.modules.amazonS3.domain.dto.buketaws;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BuketAwsDto extends AuditInfoDto {
    private Long id;
    private String projectName;
    private Integer applicationId;
    private String bucketName;
    private String bucketFolder;
}
