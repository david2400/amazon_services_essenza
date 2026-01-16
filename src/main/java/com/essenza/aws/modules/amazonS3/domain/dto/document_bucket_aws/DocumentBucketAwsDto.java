package com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentBucketAwsDto extends AuditInfoDto {
    private Long id;
    private Integer documentId;
    private Long bucketAwsId;
}
