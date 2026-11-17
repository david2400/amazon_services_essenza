package com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws;

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
public class CreateDocumentBucketAwsDto extends AuditInfoDto {
    @NotNull
    @Positive
    private Integer documentId;

    @NotNull
    @Positive
    private Long bucketAwsId;
}
