package com.essenza.aws.modules.amazonS3.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonS3.application.input.files_amazon.CreatePresignedGetUrlUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.DeleteFileByKeyAndBucketUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.DownloadFileUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByBucketAwsIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByDocumentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByParentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.LinkFileToBucketUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.ReplaceFileUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UpdateDocumentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UpdateFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UploadFileUseCase;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.shared.exceptions.AwsServiceException;
import com.essenza.aws.shared.exceptions.GlobalExceptionHandler;
import com.essenza.aws.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP que consume el servicio notificacion (biblioteca de imágenes de correo).
 * Las rutas reales llevan el prefijo /api/aws (ApiPrefixConfiguration); aquí se prueban sin él.
 */
class FilesAmazonS3ControllerTest {

    private static final String KEY = "email/imagenes/2026/11/3f2a9c1e-0000-4000-8000-000000000001.png";
    private static final String URL = "https://essenza-correos.s3.us-east-1.amazonaws.com/" + KEY;

    private UploadFileUseCase uploadUseCase;
    private DeleteFileByKeyAndBucketUseCase deleteUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        uploadUseCase = mock(UploadFileUseCase.class);
        deleteUseCase = mock(DeleteFileByKeyAndBucketUseCase.class);
        FilesAmazonS3Controller controller = new FilesAmazonS3Controller(
                uploadUseCase,
                mock(DownloadFileUseCase.class),
                mock(CreatePresignedGetUrlUseCase.class),
                mock(ReplaceFileUseCase.class),
                mock(UpdateDocumentIdUseCase.class),
                deleteUseCase,
                mock(UpdateFilesAmazonS3UseCase.class),
                mock(FindFilesAmazonS3UseCase.class),
                mock(FindFilesAmazonS3ByDocumentIdUseCase.class),
                mock(FindFilesAmazonS3ByBucketAwsIdUseCase.class),
                mock(FindFilesAmazonS3ByParentIdUseCase.class),
                mock(LinkFileToBucketUseCase.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AmazonS3ExceptionHandler(), new GlobalExceptionHandler())
                .build();
    }

    @Test
    void subirArchivoDevuelveIdKeyYS3UrlEnCamelCase() throws Exception {
        when(uploadUseCase.upload(any(), eq("essenza-correos"), eq("email/imagenes/2026/11"), any()))
                .thenReturn(FilesAmazonS3Dto.builder().id(42L).key(KEY).s3Url(URL).bucketAwsId(7L)
                        .fileName("3f2a9c1e-0000-4000-8000-000000000001.png").build());

        mockMvc.perform(multipart("/files-amazon/subir_archivo")
                        .file(new MockMultipartFile("file", "3f2a9c1e-0000-4000-8000-000000000001.png", "image/png", new byte[]{1}))
                        .param("bucket", "essenza-correos")
                        .param("carpeta", "email/imagenes/2026/11")
                        .param("bucket_aws_id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.key").value(KEY))
                .andExpect(jsonPath("$.s3Url").value(URL));
    }

    @Test
    void subirConBucketAjenoResponde400() throws Exception {
        when(uploadUseCase.upload(any(), anyString(), anyString(), any()))
                .thenThrow(new IllegalArgumentException("El bucket indicado no corresponde al bucket_aws_id 7"));

        mockMvc.perform(multipart("/files-amazon/subir_archivo")
                        .file(new MockMultipartFile("file", "a.png", "image/png", new byte[]{1}))
                        .param("bucket", "otro-bucket")
                        .param("carpeta", "email/imagenes/2026/11")
                        .param("bucket_aws_id", "7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void eliminarArchivoInexistenteResponde404() throws Exception {
        when(deleteUseCase.deleteByKeyAndBucket(eq(KEY), eq(7L)))
                .thenThrow(new NotFoundException("No se encontró archivo con key " + KEY + " y bucketAwsId 7"));

        mockMvc.perform(delete("/files-amazon/eliminar_archivo").param("clave", KEY).param("bucketAwsId", "7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void errorDeS3AlEliminarResponde502SinTrazas() throws Exception {
        when(deleteUseCase.deleteByKeyAndBucket(eq(KEY), eq(7L)))
                .thenThrow(new AwsServiceException("Failed to delete object from S3", "InternalError",
                        new RuntimeException("detalle interno del SDK")));

        mockMvc.perform(delete("/files-amazon/eliminar_archivo").param("clave", KEY).param("bucketAwsId", "7"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Failed to delete object from S3"))
                .andExpect(content().string(not(containsString("detalle interno"))))
                .andExpect(content().string(not(containsString("Exception"))));
    }
}
