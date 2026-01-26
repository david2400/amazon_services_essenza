package com.essenza.aws.modules.amazonS3.application.input.files_amazon;

import com.essenza.aws.modules.amazonS3.domain.model.S3ObjectStream;

public interface DownloadFileUseCase {
    S3ObjectStream download(String bucket, String key);
}
