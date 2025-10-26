package com.essenza.aws.modules.amazonS3.application.input.filesAmazon;

public interface DownloadFileUseCase {
    byte[] download(String bucket, String key);
}
