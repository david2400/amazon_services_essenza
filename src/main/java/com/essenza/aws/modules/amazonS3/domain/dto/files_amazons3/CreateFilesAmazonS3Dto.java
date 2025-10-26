package com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFilesAmazonS3Dto  extends AuditInfoDto {
    private Long parentId;

    private String fileName;

    @NotNull
    private String key;

    @NotNull
    @Positive
    private Integer documentId;

    @NotNull
    @Positive
    private Integer relationTypeId;

    private Date expirationDate;

    private String eTag;

    private String route;

    private String s3Url;

    private Integer reference;

    @NotNull
    @Positive
    private Long bucketAwsId;
}
