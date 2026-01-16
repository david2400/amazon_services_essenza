package com.essenza.aws.modules.amazonS3.application.input.files_amazon;

import java.time.Duration;

public interface CreatePresignedGetUrlUseCase {
    String createPresignedGetUrl(String bucket, String key, Duration ttl);
}
