package com.essenza.aws.modules.amazonS3.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;
import java.util.Map;

@Getter
@Builder
public class S3ObjectStream {

    private final InputStream inputStream;
    private final Long contentLength;
    private final String contentType;
    private final Map<String, String> metadata;
}
