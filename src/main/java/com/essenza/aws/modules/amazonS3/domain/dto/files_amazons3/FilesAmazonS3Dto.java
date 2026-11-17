package com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FilesAmazonS3Dto extends AuditInfoDto {
    private Long id;
    private Long parentId;
    private String fileName;
    private String key;
    private Integer documentId;
    private Instant expirationDate;
    private String eTag;
    private String route;
    private String s3Url;
    private Integer reference;
    private Long bucketAwsId;
    private Long s3ObjectId;
    private String contentHash;
    private Long sizeBytes;
}
