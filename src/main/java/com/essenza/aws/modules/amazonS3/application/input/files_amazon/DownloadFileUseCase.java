package com.essenza.aws.modules.amazonS3.application.input.files_amazon;

import com.essenza.aws.modules.amazonS3.domain.model.S3ObjectStream;

public interface DownloadFileUseCase {
    S3ObjectStream download(String bucket, String key);

    /**
     * Descarga una referencia por id: el servicio resuelve internamente en qué
     * bucket físico vive el objeto (puede ser el de otro proyecto vinculado).
     */
    S3ObjectStream download(Long fileId);
}
