package com.essenza.aws.modules.amazonS3.application.input.files_amazon;

public interface DownloadFileUseCase {
    byte[] download(String bucket, String key);
}
