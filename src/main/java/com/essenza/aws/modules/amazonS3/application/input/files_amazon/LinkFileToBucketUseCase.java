package com.essenza.aws.modules.amazonS3.application.input.files_amazon;

import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.LinkFilesAmazonS3Dto;

public interface LinkFileToBucketUseCase {
    /**
     * Crea una referencia lógica de un archivo existente en otro bucket registrado
     * (por ejemplo, el mismo documento usado por otro proyecto). No transfiere
     * bytes a S3: ambas referencias apuntan al mismo objeto físico.
     */
    FilesAmazonS3Dto link(Long fileId, Long targetBucketAwsId, LinkFilesAmazonS3Dto meta);
}
