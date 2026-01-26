package com.essenza.aws.modules.amazonS3.application.output.storage;

import com.essenza.aws.modules.amazonS3.domain.model.S3ObjectStream;
import com.essenza.aws.modules.amazonS3.domain.model.S3UploadResult;

import java.io.InputStream;
import java.time.Duration;

public interface S3StorageGateway {

    S3UploadResult upload(String bucket, String key, String contentType, InputStream content, long contentLength);

    S3ObjectStream download(String bucket, String key);

    void delete(String bucket, String key);

    String createPresignedGetUrl(String bucket, String key, Duration ttl);
}
