package com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * La relación se identifica por su clave compuesta (documentId, bucketAwsId);
 * este DTO queda para campos mutables futuros. Los nulos se ignoran.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class UpdateDocumentBucketAwsDto {
}
