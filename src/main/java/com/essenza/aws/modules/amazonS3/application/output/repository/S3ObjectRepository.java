package com.essenza.aws.modules.amazonS3.application.output.repository;

import com.essenza.aws.modules.amazonS3.domain.dto.s3object.S3ObjectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Registro de objetos físicos en S3 (deduplicación por contenido). */
public interface S3ObjectRepository {

    S3ObjectDto save(S3ObjectDto input);

    Optional<S3ObjectDto> findById(Long id);

    /** Busca un objeto activo con el mismo contenido dentro del mismo bucket registrado. */
    Optional<S3ObjectDto> findByBucketAwsIdAndContentHash(Long bucketAwsId, String contentHash);

    Page<S3ObjectDto> findByBucketAwsId(Long bucketAwsId, Pageable pageable);

    boolean deleteById(Long id);
}
