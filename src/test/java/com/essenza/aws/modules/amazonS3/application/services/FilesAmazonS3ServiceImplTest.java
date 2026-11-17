package com.essenza.aws.modules.amazonS3.application.services;

import com.essenza.aws.modules.amazonS3.application.output.repository.BucketAwsRepository;
import com.essenza.aws.modules.amazonS3.application.output.repository.FilesAmazonS3Repository;
import com.essenza.aws.modules.amazonS3.application.output.repository.S3ObjectRepository;
import com.essenza.aws.modules.amazonS3.application.output.storage.S3StorageGateway;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.BucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.LinkFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.s3object.S3ObjectDto;
import com.essenza.aws.modules.amazonS3.domain.model.S3UploadResult;
import com.essenza.aws.shared.config.AwsProperties;
import com.essenza.aws.shared.exceptions.NotFoundException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FilesAmazonS3ServiceImplTest {

    private static final String BUCKET = "essenza-correos";
    private static final long BUCKET_ID = 7L;

    private FilesAmazonS3Repository repository;
    private S3ObjectRepository s3ObjectRepository;
    private BucketAwsRepository bucketCatalog;
    private S3StorageGateway storageGateway;
    private FilesAmazonS3ServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(FilesAmazonS3Repository.class);
        s3ObjectRepository = mock(S3ObjectRepository.class);
        bucketCatalog = mock(BucketAwsRepository.class);
        storageGateway = mock(S3StorageGateway.class);
        service = new FilesAmazonS3ServiceImpl(repository, s3ObjectRepository, bucketCatalog, storageGateway,
                new AwsProperties(), new SimpleMeterRegistry());
    }

    // ------------------------------------------------------------------ upload

    @Test
    void subeConClaveBajoLaCarpetaYConservaElContrato() {
        registerBucket(BUCKET);
        when(s3ObjectRepository.findByBucketAwsIdAndContentHash(eq(BUCKET_ID), anyString()))
                .thenReturn(Optional.empty());
        when(storageGateway.upload(eq(BUCKET), anyString(), eq("image/png"), any(InputStream.class), eq(3L)))
                .thenAnswer(inv -> S3UploadResult.builder()
                        .key(inv.getArgument(1))
                        .eTag("\"e\"")
                        .url("https://" + BUCKET + ".s3.us-east-1.amazonaws.com/" + inv.getArgument(1))
                        .build());
        when(s3ObjectRepository.save(any(S3ObjectDto.class))).thenAnswer(inv -> {
            S3ObjectDto dto = inv.getArgument(0);
            dto.setId(50L);
            return dto;
        });
        when(repository.create(any(CreateFilesAmazonS3Dto.class))).thenAnswer(inv -> {
            CreateFilesAmazonS3Dto meta = inv.getArgument(0);
            return FilesAmazonS3Dto.builder().id(99L).key(meta.getKey()).s3Url(meta.getS3Url())
                    .bucketAwsId(meta.getBucketAwsId()).s3ObjectId(meta.getS3ObjectId())
                    .contentHash(meta.getContentHash()).build();
        });

        FilesAmazonS3Dto result = service.upload(png("6c1f0a2e-uuid.png"), BUCKET, "email/imagenes/2026/11", meta());

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getKey()).matches("email/imagenes/2026/11/[0-9a-f-]{36}\\.png");
        assertThat(result.getS3Url()).isEqualTo("https://" + BUCKET + ".s3.us-east-1.amazonaws.com/" + result.getKey());
        assertThat(result.getS3ObjectId()).isEqualTo(50L);
        assertThat(result.getContentHash()).matches("^[0-9a-f]{64}$");
    }

    @Test
    void subidaConContenidoExistenteSoloCreaReferenciaSinTocarS3() {
        registerBucket(BUCKET);
        S3ObjectDto existing = S3ObjectDto.builder()
                .id(50L).bucketAwsId(BUCKET_ID).objectKey("email/imagenes/2026/10/abc.png")
                .contentHash("a".repeat(64)).eTag("\"e\"").s3Url("https://x/abc.png").sizeBytes(3L).build();
        when(s3ObjectRepository.findByBucketAwsIdAndContentHash(eq(BUCKET_ID), anyString()))
                .thenReturn(Optional.of(existing));
        when(repository.create(any(CreateFilesAmazonS3Dto.class))).thenAnswer(inv -> {
            CreateFilesAmazonS3Dto meta = inv.getArgument(0);
            return FilesAmazonS3Dto.builder().id(100L).key(meta.getKey()).s3ObjectId(meta.getS3ObjectId()).build();
        });

        FilesAmazonS3Dto result = service.upload(png("cedula.png"), BUCKET, "email/imagenes/2026/11", meta());

        assertThat(result.getKey()).isEqualTo("email/imagenes/2026/10/abc.png");
        assertThat(result.getS3ObjectId()).isEqualTo(50L);
        verifyNoInteractions(storageGateway);
        verify(s3ObjectRepository, never()).save(any());
    }

    @Test
    void siLaPersistenciaFallaBorraElObjetoDeS3() {
        registerBucket(BUCKET);
        when(storageGateway.upload(eq(BUCKET), anyString(), anyString(), any(InputStream.class), eq(3L)))
                .thenAnswer(inv -> S3UploadResult.builder()
                        .key(inv.getArgument(1)).eTag("\"e\"").url("u").build());
        when(s3ObjectRepository.save(any(S3ObjectDto.class))).thenAnswer(inv -> {
            S3ObjectDto dto = inv.getArgument(0);
            dto.setId(50L);
            return dto;
        });
        when(repository.create(any())).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.upload(png("a.png"), BUCKET, "email/imagenes", meta()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db down");

        verify(storageGateway).delete(eq(BUCKET), anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"../otro", "email\\imagenes", "email//imagenes", "email/imagenes\r\n"})
    void rechazaCarpetaInvalidaSinTocarS3(String carpeta) {
        assertThatThrownBy(() -> service.upload(png("a.png"), BUCKET, carpeta, meta()))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(storageGateway, repository, s3ObjectRepository);
    }

    @Test
    void rechazaBucketQueNoCoincideConElRegistrado() {
        registerBucket("bucket-de-otro-proyecto");

        assertThatThrownBy(() -> service.upload(png("a.png"), BUCKET, "email/imagenes/2026/11", meta()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no corresponde al bucket_aws_id " + BUCKET_ID);

        verifyNoInteractions(storageGateway, repository, s3ObjectRepository);
    }

    @Test
    void rechazaBucketAwsIdNoRegistrado() {
        when(bucketCatalog.findById(BUCKET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upload(png("a.png"), BUCKET, "email/imagenes/2026/11", meta()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está registrado");

        verifyNoInteractions(storageGateway, repository, s3ObjectRepository);
    }

    // ------------------------------------------------------------- presigned URL

    @Test
    void rechazaTtlMayorAlMaximoConfigurado() {
        when(bucketCatalog.findByBucketName(BUCKET)).thenReturn(Optional.of(
                BucketAwsDto.builder().id(BUCKET_ID).bucketName(BUCKET).build()));

        assertThatThrownBy(() -> service.createPresignedGetUrl(BUCKET, "k.png", Duration.ofDays(30)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TTL");

        verify(storageGateway, never()).createPresignedGetUrl(anyString(), anyString(), any());
    }

    @Test
    void rechazaDescargaDeBucketNoRegistrado() {
        when(bucketCatalog.findByBucketName("bucket-ajeno")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.download("bucket-ajeno", "k.png"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(storageGateway);
    }

    // ------------------------------------------------------------------ delete

    @Test
    void borrarClaveInexistenteLanzaNotFoundSinTocarS3() {
        when(repository.findByKeyAndBucketAwsId("email/imagenes/x.png", BUCKET_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> service.deleteByKeyAndBucket("email/imagenes/x.png", BUCKET_ID))
                .isInstanceOf(NotFoundException.class);

        verify(storageGateway, never()).delete(anyString(), anyString());
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void borrarEliminaEnS3TodasLasReferenciasYElObjeto() {
        String key = "email/imagenes/2026/11/uuid.png";
        FilesAmazonS3Dto refA = FilesAmazonS3Dto.builder().id(5L).key(key).bucketAwsId(BUCKET_ID).s3ObjectId(50L).build();
        FilesAmazonS3Dto refB = FilesAmazonS3Dto.builder().id(6L).key(key).bucketAwsId(BUCKET_ID).s3ObjectId(50L).build();
        when(repository.findByKeyAndBucketAwsId(key, BUCKET_ID)).thenReturn(List.of(refA, refB));
        when(bucketCatalog.findById(BUCKET_ID)).thenReturn(Optional.of(
                BucketAwsDto.builder().id(BUCKET_ID).bucketName(BUCKET).build()));

        FilesAmazonS3Dto result = service.deleteByKeyAndBucket(key, BUCKET_ID);

        assertThat(result.getKey()).isEqualTo(key);
        verify(storageGateway).delete(BUCKET, key);
        verify(repository).deleteById(5L);
        verify(repository).deleteById(6L);
        verify(s3ObjectRepository).deleteById(50L);
    }

    @Test
    void deleteByIdLiberaElObjetoSoloSinQuedanReferencias() {
        FilesAmazonS3Dto file = FilesAmazonS3Dto.builder().id(5L).key("k.png").bucketAwsId(BUCKET_ID).s3ObjectId(50L).build();
        when(repository.findById(5L)).thenReturn(Optional.of(file));
        when(repository.deleteById(5L)).thenReturn(true);
        when(repository.countByS3ObjectId(50L)).thenReturn(0L);
        when(s3ObjectRepository.findById(50L)).thenReturn(Optional.of(
                S3ObjectDto.builder().id(50L).bucketAwsId(BUCKET_ID).objectKey("k.png").build()));
        when(bucketCatalog.findById(BUCKET_ID)).thenReturn(Optional.of(
                BucketAwsDto.builder().id(BUCKET_ID).bucketName(BUCKET).build()));

        assertThat(service.deleteById(5L)).isTrue();

        verify(storageGateway).delete(BUCKET, "k.png");
        verify(s3ObjectRepository).deleteById(50L);
    }

    @Test
    void deleteByIdConservaElObjetoSiHayOtrasReferencias() {
        FilesAmazonS3Dto file = FilesAmazonS3Dto.builder().id(5L).key("k.png").bucketAwsId(BUCKET_ID).s3ObjectId(50L).build();
        when(repository.findById(5L)).thenReturn(Optional.of(file));
        when(repository.deleteById(5L)).thenReturn(true);
        when(repository.countByS3ObjectId(50L)).thenReturn(1L);

        assertThat(service.deleteById(5L)).isTrue();

        verify(storageGateway, never()).delete(anyString(), anyString());
        verify(s3ObjectRepository, never()).deleteById(anyLong());
    }

    // ------------------------------------------------------------------- link

    @Test
    void vincularCreaReferenciaEnOtroBucketSinTocarS3() {
        FilesAmazonS3Dto source = FilesAmazonS3Dto.builder()
                .id(5L).key("email/imagenes/abc.png").fileName("abc.png").eTag("\"e\"")
                .s3Url("https://x/abc.png").bucketAwsId(BUCKET_ID).s3ObjectId(50L)
                .contentHash("a".repeat(64)).sizeBytes(3L).documentId(7).build();
        when(repository.findById(5L)).thenReturn(Optional.of(source));
        when(bucketCatalog.findById(99L)).thenReturn(Optional.of(
                BucketAwsDto.builder().id(99L).bucketName("essenza-subsidio").build()));
        when(repository.create(any(CreateFilesAmazonS3Dto.class))).thenAnswer(inv -> {
            CreateFilesAmazonS3Dto meta = inv.getArgument(0);
            return FilesAmazonS3Dto.builder().id(101L).key(meta.getKey())
                    .bucketAwsId(meta.getBucketAwsId()).s3ObjectId(meta.getS3ObjectId())
                    .documentId(meta.getDocumentId()).build();
        });

        FilesAmazonS3Dto linked = service.link(5L, 99L, LinkFilesAmazonS3Dto.builder().documentId(7).parentId(42L).build());

        assertThat(linked.getBucketAwsId()).isEqualTo(99L);
        assertThat(linked.getS3ObjectId()).isEqualTo(50L);
        assertThat(linked.getKey()).isEqualTo("email/imagenes/abc.png");
        verifyNoInteractions(storageGateway);
    }

    @Test
    void vincularRechazaBucketDestinoNoRegistrado() {
        when(repository.findById(5L)).thenReturn(Optional.of(
                FilesAmazonS3Dto.builder().id(5L).key("k.png").bucketAwsId(BUCKET_ID).build()));
        when(bucketCatalog.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.link(5L, 99L, new LinkFilesAmazonS3Dto()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está registrado");

        verify(repository, never()).create(any());
    }

    @Test
    void vincularArchivoInexistenteLanzaNotFound() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.link(5L, 99L, new LinkFilesAmazonS3Dto()))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).create(any());
    }

    // ----------------------------------------------------------------- helpers

    private void registerBucket(String bucketName) {
        when(bucketCatalog.findById(anyLong())).thenReturn(Optional.of(
                BucketAwsDto.builder().id(BUCKET_ID).bucketName(bucketName).bucketFolder("email/imagenes").build()));
    }

    private static CreateFilesAmazonS3Dto meta() {
        return CreateFilesAmazonS3Dto.builder().bucketAwsId(BUCKET_ID).build();
    }

    private static MockMultipartFile png(String name) {
        return new MockMultipartFile("file", name, "image/png", new byte[]{1, 2, 3});
    }
}
