package com.essenza.aws.modules.amazonS3.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class S3UploadResult {

    private final String key;
    private final String eTag;
    private final String url;
}
