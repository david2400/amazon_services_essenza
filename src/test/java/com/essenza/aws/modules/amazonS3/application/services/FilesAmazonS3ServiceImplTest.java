package com.essenza.aws.modules.amazonS3.application.services;

import com.essenza.aws.modules.amazonS3.application.output.repository.BuketAwsRepository;
import com.essenza.aws.modules.amazonS3.application.output.storage.S3StorageGateway;
import com.essenza.aws.modules.amazonS3.domain.dto.buketaws.BuketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.exception.ResourceNotFoundException;
import com.essenza.aws.modules.amazonS3.domain.model.S3UploadResult;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers.FilesAmazonS3Mapper;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.BuketAwsEntity;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.FilesAmazonS3Entity;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.buketaws.JpaBuketAwsRepository;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.files_amazon.FilesAmazonS3RepositoryAdapter;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.files_amazon.JpaFilesAmazonS3Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
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

    private FilesAmazonS3RepositoryAdapter repository;
    private JpaBuketAwsRepository jpaBuketAwsRepository;
    private JpaFilesAmazonS3Repository jpaFilesRepository;
    private FilesAmazonS3Mapper mapper;
    private S3StorageGateway storageGateway;
    private BuketAwsRepository bucketCatalog;
    private FilesAmazonS3ServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(FilesAmazonS3RepositoryAdapter.class);
        jpaBuketAwsRepository = mock(JpaBuketAwsRepository.class);
        jpaFilesRepository = mock(JpaFilesAmazonS3Repository.class);
        mapper = mock(FilesAmazonS3Mapper.class);
        storageGateway = mock(S3StorageGateway.class);
        bucketCatalog = mock(BuketAwsRepository.class);
        service = new FilesAmazonS3ServiceImpl(repository, jpaBuketAwsRepository, jpaFilesRepository, mapper,
                storageGateway, bucketCatalog);
    }

    // ------------------------------------------------------------------ upload

    @Test
    void subeConClaveBajoLaCarpetaYConservaElContrato() {
        registerBucket(BUCKET);
        when(storageGateway.upload(eq(BUCKET), anyString(), eq("image/png"), any(InputStream.class), eq(3L)))
                .thenAnswer(inv -> S3UploadResult.builder()
                        .key(inv.getArgument(1))
                        .eTag("\"e\"")
                        .url("https://" + BUCKET + ".s3.us-east-1.amazonaws.com/" + inv.getArgument(1))
                        .build());
        when(repository.create(any(CreateFilesAmazonS3Dto.class))).thenAnswer(inv -> {
            CreateFilesAmazonS3Dto meta = inv.getArgument(0);
            return FilesAmazonS3Dto.builder().id(99L).key(meta.getKey()).s3Url(meta.getS3Url())
                    .bucketAwsId(meta.getBucketAwsId()).build();
        });

        FilesAmazonS3Dto result = service.upload(png("6c1f0a2e-uuid.png"), BUCKET, "email/imagenes/2026/11", meta(), "0");

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getKey()).matches("email/imagenes/2026/11/[0-9a-f-]{36}\\.png");
        assertThat(result.getS3Url()).isEqualTo("https://" + BUCKET + ".s3.us-east-1.amazonaws.com/" + result.getKey());
    }

    @ParameterizedTest
    @ValueSource(strings = {"../otro", "email\\imagenes", "email//imagenes", "email/imagenes\r\n"})
    void rechazaCarpetaInvalidaSinTocarS3(String carpeta) {
        assertThatThrownBy(() -> service.upload(png("a.png"), BUCKET, carpeta, meta(), "0"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(storageGateway, repository);
    }

    @Test
    void rechazaBucketQueNoCoincideConElRegistrado() {
        registerBucket("bucket-de-otro-proyecto");

        assertThatThrownBy(() -> service.upload(png("a.png"), BUCKET, "email/imagenes/2026/11", meta(), "0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no corresponde al buket_aws_id " + BUCKET_ID);

        verifyNoInteractions(storageGateway, repository);
    }

    @Test
    void rechazaBuketAwsIdNoRegistrado() {
        when(bucketCatalog.findById(BUCKET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upload(png("a.png"), BUCKET, "email/imagenes/2026/11", meta(), "0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está registrado");

        verifyNoInteractions(storageGateway, repository);
    }

    // ------------------------------------------------------------------ delete

    @Test
    void borrarClaveInexistenteLanzaNotFoundSinTocarS3() {
        when(jpaFilesRepository.findByKeyAndBucketAwsId("email/imagenes/x.png", BUCKET_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> service.deleteByKeyAndBucket("email/imagenes/x.png", BUCKET_ID, "0"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(storageGateway, never()).delete(anyString(), anyString());
        verify(jpaFilesRepository, never()).delete(any(FilesAmazonS3Entity.class));
    }

    @Test
    void borrarEliminaEnS3YMarcaLaFilaComoEliminada() {
        String key = "email/imagenes/2026/11/uuid.png";
        FilesAmazonS3Entity entity = new FilesAmazonS3Entity();
        entity.setKey(key);
        BuketAwsEntity bucket = new BuketAwsEntity();
        bucket.setBucketName(BUCKET);
        when(jpaFilesRepository.findByKeyAndBucketAwsId(key, BUCKET_ID)).thenReturn(List.of(entity));
        when(jpaBuketAwsRepository.findById(BUCKET_ID)).thenReturn(Optional.of(bucket));
        when(mapper.toDto(entity)).thenReturn(FilesAmazonS3Dto.builder().id(5L).key(key).build());

        List<FilesAmazonS3Dto> result = service.deleteByKeyAndBucket(key, BUCKET_ID, "0");

        assertThat(result).extracting(FilesAmazonS3Dto::getKey).containsExactly(key);
        verify(storageGateway).delete(BUCKET, key);
        ArgumentCaptor<FilesAmazonS3Entity> deleted = ArgumentCaptor.forClass(FilesAmazonS3Entity.class);
        verify(jpaFilesRepository).delete(deleted.capture());
        assertThat(deleted.getValue()).isSameAs(entity);
    }

    // ----------------------------------------------------------------- helpers

    private void registerBucket(String bucketName) {
        when(bucketCatalog.findById(anyLong())).thenReturn(Optional.of(
                BuketAwsDto.builder().id(BUCKET_ID).bucketName(bucketName).bucketFolder("email/imagenes").build()));
    }

    private static CreateFilesAmazonS3Dto meta() {
        return CreateFilesAmazonS3Dto.builder().bucketAwsId(BUCKET_ID).build();
    }

    private static MockMultipartFile png(String name) {
        return new MockMultipartFile("file", name, "image/png", new byte[]{1, 2, 3});
    }
}
