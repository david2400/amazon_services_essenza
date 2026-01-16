package com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws;

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
public class CreateDocumentBucketAwsDto extends AuditInfoDto {
    @NotNull
    private Integer documentId;

    @NotNull
    private Long bucketAwsId;
}
