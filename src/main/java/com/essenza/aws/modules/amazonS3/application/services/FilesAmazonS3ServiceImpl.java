package com.essenza.aws.modules.amazonS3.application.services;

import com.essenza.aws.modules.amazonS3.application.input.files_amazon.CreateFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.CreatePresignedGetUrlUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.DeleteFileByKeyAndBucketUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.DeleteFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.DownloadFileUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByBucketAwsIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByDocumentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByParentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.LinkFileToBucketUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.ReplaceFileUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UpdateDocumentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UpdateFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UploadFileUseCase;
import com.essenza.aws.modules.amazonS3.application.output.repository.BucketAwsRepository;
import com.essenza.aws.modules.amazonS3.application.output.repository.FilesAmazonS3Repository;
import com.essenza.aws.modules.amazonS3.application.output.repository.S3ObjectRepository;
import com.essenza.aws.modules.amazonS3.application.output.storage.S3StorageGateway;
import com.essenza.aws.modules.amazonS3.domain.dto.bucketaws.BucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.LinkFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.UpdateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.s3object.S3ObjectDto;
import com.essenza.aws.modules.amazonS3.domain.model.S3ObjectStream;
import com.essenza.aws.modules.amazonS3.domain.model.S3UploadResult;
import com.essenza.aws.modules.amazonS3.domain.validation.S3FolderValidator;
import com.essenza.aws.shared.config.AwsProperties;
import com.essenza.aws.shared.exceptions.AwsServiceException;
import com.essenza.aws.shared.exceptions.NotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Las llamadas a AWS S3 se ejecutan FUERA de transacciones de base de datos: los métodos que
 * hablan con S3 no son transaccionales y la persistencia se delega a los puertos, que manejan
 * su propia transacción por operación.
 *
 * <p><b>Deduplicación por contenido</b>: cada objeto físico se registra una vez en
 * {@code s3_objects} (SHA-256 + bucket). Si el contenido ya existe en el bucket registrado,
 * la subida solo crea una nueva referencia lógica en {@code amazon_s3_files} sin transferir
 * bytes a S3. El objeto físico se borra de S3 únicamente cuando su última referencia activa
 * desaparece (ref-count).
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FilesAmazonS3ServiceImpl implements
        UploadFileUseCase,
        DownloadFileUseCase,
        CreatePresignedGetUrlUseCase,
        LinkFileToBucketUseCase,
        ReplaceFileUseCase,
        UpdateDocumentIdUseCase,
        DeleteFileByKeyAndBucketUseCase,
        CreateFilesAmazonS3UseCase,
        UpdateFilesAmazonS3UseCase,
        DeleteFilesAmazonS3UseCase,
        FindFilesAmazonS3ByIdUseCase,
        FindFilesAmazonS3UseCase,
        FindFilesAmazonS3ByDocumentIdUseCase,
        FindFilesAmazonS3ByBucketAwsIdUseCase,
        FindFilesAmazonS3ByParentIdUseCase {

    private final FilesAmazonS3Repository repository;
    private final S3ObjectRepository s3ObjectRepository;
    private final BucketAwsRepository bucketCatalog;
    private final S3StorageGateway storageGateway;
    private final AwsProperties awsProperties;
    private final MeterRegistry meterRegistry;

    @Override
    public FilesAmazonS3Dto create(CreateFilesAmazonS3Dto input) {
        return repository.create(input);
    }

    @Override
    public FilesAmazonS3Dto update(Long id, UpdateFilesAmazonS3Dto input) {
        return repository.update(id, input);
    }

    @Override
    public boolean deleteById(Long id) {
        FilesAmazonS3Dto file = repository.findById(id).orElse(null);
        if (file == null) {
            return false;
        }
        boolean deleted = repository.deleteById(id);
        if (deleted) {
            releaseObject(file.getS3ObjectId());
        }
        return deleted;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FilesAmazonS3Dto> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FilesAmazonS3Dto> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FilesAmazonS3Dto> findByDocumentId(Integer documentId, Pageable pageable) {
        return repository.findByDocumentId(documentId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FilesAmazonS3Dto> findByBucketAwsId(Long bucketAwsId, Pageable pageable) {
        return repository.findByBucketAwsId(bucketAwsId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FilesAmazonS3Dto> findByParentId(Long parentId, Pageable pageable) {
        return repository.findByParentId(parentId, pageable);
    }

    // --- S3 logic methods (Use cases) ---

    @Override
    public FilesAmazonS3Dto upload(MultipartFile file,
                                   String bucket,
                                   String folder,
                                   CreateFilesAmazonS3Dto meta) {
        S3FolderValidator.validate(folder);
        validateBucketMatchesRegistry(bucket, meta.getBucketAwsId());

        String contentHash = sha256(file);

        // Dedup por contenido dentro del mismo bucket registrado: cero bytes transferidos.
        Optional<S3ObjectDto> existing =
                s3ObjectRepository.findByBucketAwsIdAndContentHash(meta.getBucketAwsId(), contentHash);
        if (existing.isPresent()) {
            S3ObjectDto object = existing.get();
            fillMetaFromObject(meta, object, file);
            FilesAmazonS3Dto saved = repository.create(meta);
            meterRegistry.counter("s3.files.upload", "result", "deduplicated").increment();
            log.info("Upload deduplicated: reference {} points to existing s3_object {} (s3://{}/{})",
                    saved.getId(), object.getId(), bucket, object.getObjectKey());
            return saved;
        }

        String fileName = buildFileName(file);
        String key = buildObjectKey(folder, fileName);

        try (InputStream content = file.getInputStream()) {
            S3UploadResult uploadResult = storageGateway.upload(
                    bucket,
                    key,
                    file.getContentType(),
                    content,
                    file.getSize()
            );

            try {
                S3ObjectDto object = s3ObjectRepository.save(S3ObjectDto.builder()
                        .bucketAwsId(meta.getBucketAwsId())
                        .objectKey(key)
                        .contentHash(contentHash)
                        .sizeBytes(file.getSize())
                        .contentType(file.getContentType())
                        .eTag(uploadResult.getETag())
                        .s3Url(uploadResult.getUrl())
                        .build());

                meta.setFileName(fileName);
                meta.setKey(key);
                meta.setETag(uploadResult.getETag());
                meta.setS3Url(uploadResult.getUrl());
                meta.setS3ObjectId(object.getId());
                meta.setContentHash(contentHash);
                meta.setSizeBytes(file.getSize());

                FilesAmazonS3Dto saved = repository.create(meta);
                meterRegistry.counter("s3.files.upload", "result", "success").increment();
                log.info("File uploaded to s3://{}/{} (id={})", bucket, key, saved.getId());
                return saved;
            } catch (RuntimeException e) {
                // Compensación: si la persistencia falla, no dejar el objeto huérfano en S3.
                deleteQuietly(bucket, key);
                throw e;
            }
        } catch (IOException e) {
            meterRegistry.counter("s3.files.upload", "result", "error").increment();
            throw new AwsServiceException("Failed to read file content for upload to S3", "S3_IO_ERROR", e);
        }
    }

    @Override
    public S3ObjectStream download(String bucket, String key) {
        validateBucketRegistered(bucket);
        meterRegistry.counter("s3.files.download", "result", "success").increment();
        return storageGateway.download(bucket, key);
    }

    @Override
    public S3ObjectStream download(Long fileId) {
        ObjectLocation location = resolveLocation(loadFile(fileId));
        meterRegistry.counter("s3.files.download", "result", "success").increment();
        return storageGateway.download(location.bucketName(), location.objectKey());
    }

    @Override
    public String createPresignedGetUrl(String bucket, String key, Duration ttl) {
        validateBucketRegistered(bucket);
        return storageGateway.createPresignedGetUrl(bucket, key, boundedTtl(ttl));
    }

    @Override
    public String createPresignedGetUrl(Long fileId, Duration ttl) {
        ObjectLocation location = resolveLocation(loadFile(fileId));
        return storageGateway.createPresignedGetUrl(location.bucketName(), location.objectKey(), boundedTtl(ttl));
    }

    @Override
    public FilesAmazonS3Dto link(Long fileId, Long targetBucketAwsId, LinkFilesAmazonS3Dto input) {
        FilesAmazonS3Dto source = loadFile(fileId);
        if (bucketCatalog.findById(targetBucketAwsId).isEmpty()) {
            throw new IllegalArgumentException("bucket_aws_id " + targetBucketAwsId + " no está registrado");
        }

        CreateFilesAmazonS3Dto meta = CreateFilesAmazonS3Dto.builder()
                .fileName(source.getFileName())
                .key(source.getKey())
                .eTag(source.getETag())
                .s3Url(source.getS3Url())
                .route(source.getRoute())
                .documentId(input.getDocumentId() != null ? input.getDocumentId() : source.getDocumentId())
                .parentId(input.getParentId() != null ? input.getParentId() : source.getParentId())
                .reference(input.getReference() != null ? input.getReference() : source.getReference())
                .expirationDate(input.getExpirationDate() != null ? input.getExpirationDate() : source.getExpirationDate())
                .bucketAwsId(targetBucketAwsId)
                .s3ObjectId(source.getS3ObjectId())
                .contentHash(source.getContentHash())
                .sizeBytes(source.getSizeBytes())
                .build();

        FilesAmazonS3Dto linked = repository.create(meta);
        meterRegistry.counter("s3.files.link", "result", "success").increment();
        log.info("File {} linked to bucket_aws_id {} as reference {} (s3_object {})",
                fileId, targetBucketAwsId, linked.getId(), source.getS3ObjectId());
        return linked;
    }

    @Override
    public FilesAmazonS3Dto replace(Long id, MultipartFile file) {
        FilesAmazonS3Dto current = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Archivo con id " + id + " no existe"));

        BucketAwsDto bucket = bucketCatalog.findById(current.getBucketAwsId())
                .orElseThrow(() -> new NotFoundException("Bucket no encontrado con id " + current.getBucketAwsId()));

        String bucketName = bucket.getBucketName();
        String folder = bucket.getBucketFolder() != null ? bucket.getBucketFolder() : "";
        String contentHash = sha256(file);

        UpdateFilesAmazonS3Dto upd = new UpdateFilesAmazonS3Dto();
        Long createdObjectId = null;
        String createdObjectKey = null;

        // Dedup también en el reemplazo: si el contenido ya existe, solo se repunta la referencia.
        Optional<S3ObjectDto> existing =
                s3ObjectRepository.findByBucketAwsIdAndContentHash(current.getBucketAwsId(), contentHash);
        if (existing.isPresent()) {
            S3ObjectDto object = existing.get();
            upd.setFileName(fileName(file));
            upd.setKey(object.getObjectKey());
            upd.setETag(object.getETag());
            upd.setS3Url(object.getS3Url());
            upd.setS3ObjectId(object.getId());
            upd.setContentHash(object.getContentHash());
            upd.setSizeBytes(object.getSizeBytes());
        } else {
            String newName = buildFileName(file);
            String newKey = buildObjectKey(folder, newName);

            try (InputStream content = file.getInputStream()) {
                // Primero se sube el nuevo objeto: si falla, el archivo anterior sigue intacto.
                S3UploadResult uploadResult = storageGateway.upload(
                        bucketName,
                        newKey,
                        file.getContentType(),
                        content,
                        file.getSize()
                );

                S3ObjectDto object = s3ObjectRepository.save(S3ObjectDto.builder()
                        .bucketAwsId(current.getBucketAwsId())
                        .objectKey(newKey)
                        .contentHash(contentHash)
                        .sizeBytes(file.getSize())
                        .contentType(file.getContentType())
                        .eTag(uploadResult.getETag())
                        .s3Url(uploadResult.getUrl())
                        .build());
                createdObjectId = object.getId();
                createdObjectKey = newKey;

                upd.setFileName(newName);
                upd.setKey(newKey);
                upd.setETag(uploadResult.getETag());
                upd.setS3Url(uploadResult.getUrl());
                upd.setS3ObjectId(object.getId());
                upd.setContentHash(contentHash);
                upd.setSizeBytes(file.getSize());
            } catch (IOException e) {
                meterRegistry.counter("s3.files.replace", "result", "error").increment();
                throw new AwsServiceException("Failed to read file content for replace in S3", "S3_IO_ERROR", e);
            } catch (RuntimeException e) {
                // Compensación: si no se pudo registrar el objeto, borrar lo subido.
                deleteQuietly(bucketName, newKey);
                throw e;
            }
        }

        FilesAmazonS3Dto updated;
        try {
            updated = repository.update(id, upd);
        } catch (RuntimeException e) {
            // Compensación: el objeto nuevo queda huérfano si la referencia no se actualizó.
            if (createdObjectId != null) {
                deleteQuietly(bucketName, createdObjectKey);
                s3ObjectRepository.deleteById(createdObjectId);
            }
            throw e;
        }
        // El objeto anterior queda libre solo después de que la referencia se actualizó.
        releaseObject(current.getS3ObjectId());
        meterRegistry.counter("s3.files.replace", "result", "success").increment();
        log.info("File {} replaced (dedup={})", id, existing.isPresent());
        return updated;
    }

    @Override
    public FilesAmazonS3Dto updateDocumentId(Long id, Integer documentId) {
        UpdateFilesAmazonS3Dto upd = new UpdateFilesAmazonS3Dto();
        upd.setDocumentId(documentId);
        return repository.update(id, upd);
    }

    @Override
    public FilesAmazonS3Dto deleteByKeyAndBucket(String key, Long bucketAwsId) {
        List<FilesAmazonS3Dto> refs = repository.findByKeyAndBucketAwsId(key, bucketAwsId);
        if (refs.isEmpty()) {
            throw new NotFoundException(
                    "No se encontró archivo con key " + key + " y bucketAwsId " + bucketAwsId);
        }

        BucketAwsDto bucket = bucketCatalog.findById(bucketAwsId)
                .orElseThrow(() -> new NotFoundException("Bucket no encontrado con id " + bucketAwsId));

        // Borrar por clave elimina el objeto físico y todas sus referencias lógicas de esa clave.
        storageGateway.delete(bucket.getBucketName(), key);
        refs.forEach(ref -> repository.deleteById(ref.getId()));
        refs.stream()
                .map(FilesAmazonS3Dto::getS3ObjectId)
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(s3ObjectRepository::deleteById);

        meterRegistry.counter("s3.files.delete", "result", "success").increment();
        log.info("File deleted from s3://{}/{} ({} referencias)", bucket.getBucketName(), key, refs.size());
        return refs.get(0);
    }

    /**
     * Libera el objeto físico si ya no quedan referencias activas apuntando a él:
     * borra el objeto de S3 y marca su registro como eliminado. Filas históricas sin
     * {@code s3_object_id} se ignoran (mantienen el comportamiento anterior).
     */
    private void releaseObject(Long s3ObjectId) {
        if (s3ObjectId == null || repository.countByS3ObjectId(s3ObjectId) > 0) {
            return;
        }
        s3ObjectRepository.findById(s3ObjectId).ifPresent(object -> {
            bucketCatalog.findById(object.getBucketAwsId()).ifPresent(bucket ->
                    deleteQuietly(bucket.getBucketName(), object.getObjectKey()));
            s3ObjectRepository.deleteById(s3ObjectId);
            meterRegistry.counter("s3.objects.released", "result", "success").increment();
            log.info("s3_object {} released (no references left)", s3ObjectId);
        });
    }

    /** Copia los datos del objeto físico existente a la nueva referencia lógica. */
    private void fillMetaFromObject(CreateFilesAmazonS3Dto meta, S3ObjectDto object, MultipartFile file) {
        meta.setFileName(fileName(file));
        meta.setKey(object.getObjectKey());
        meta.setETag(object.getETag());
        meta.setS3Url(object.getS3Url());
        meta.setS3ObjectId(object.getId());
        meta.setContentHash(object.getContentHash());
        meta.setSizeBytes(object.getSizeBytes());
    }

    /** SHA-256 del contenido en hexadecimal. Lee el stream sin cargarlo en memoria. */
    private String sha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new DigestInputStream(file.getInputStream(), digest)) {
                in.transferTo(OutputStreamNull.INSTANCE);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en la JVM", e);
        } catch (IOException e) {
            meterRegistry.counter("s3.files.upload", "result", "error").increment();
            throw new AwsServiceException("Failed to read file content for hashing", "S3_IO_ERROR", e);
        }
    }

    private String fileName(MultipartFile file) {
        return file.getOriginalFilename() != null ? file.getOriginalFilename() : buildFileName(file);
    }

    /**
     * Evita subir a un bucket distinto del registrado para {@code bucket_aws_id}.
     *
     * @throws IllegalArgumentException (400) si el id no existe o el bucket no coincide.
     */
    private void validateBucketMatchesRegistry(String bucket, Long bucketAwsId) {
        BucketAwsDto registered = bucketCatalog.findById(bucketAwsId)
                .orElseThrow(() -> new IllegalArgumentException("bucket_aws_id " + bucketAwsId + " no está registrado"));
        if (!Objects.equals(registered.getBucketName(), bucket)) {
            throw new IllegalArgumentException("El bucket indicado no corresponde al bucket_aws_id " + bucketAwsId);
        }
    }

    /**
     * Solo se puede leer/pre-firmar objetos de buckets registrados en el catálogo,
     * para no exponer buckets arbitrarios accesibles con las credenciales del servicio.
     */
    private void validateBucketRegistered(String bucket) {
        if (bucketCatalog.findByBucketName(bucket).isEmpty()) {
            throw new IllegalArgumentException("El bucket '" + bucket + "' no está registrado");
        }
    }

    private FilesAmazonS3Dto loadFile(Long fileId) {
        return repository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("Archivo con id " + fileId + " no existe"));
    }

    /**
     * Resuelve el bucket y la clave físicos de una referencia. Si la referencia
     * apunta a un {@code s3_object} (dedup/vínculo), la ubicación es la del objeto
     * — que puede ser el bucket de otro proyecto. Filas históricas sin objeto
     * usan su propio key/bucket.
     */
    private ObjectLocation resolveLocation(FilesAmazonS3Dto file) {
        if (file.getS3ObjectId() != null) {
            S3ObjectDto object = s3ObjectRepository.findById(file.getS3ObjectId())
                    .orElseThrow(() -> new NotFoundException("Objeto físico " + file.getS3ObjectId() + " no existe"));
            BucketAwsDto bucket = bucketCatalog.findById(object.getBucketAwsId())
                    .orElseThrow(() -> new NotFoundException("Bucket no encontrado con id " + object.getBucketAwsId()));
            return new ObjectLocation(bucket.getBucketName(), object.getObjectKey());
        }
        BucketAwsDto bucket = bucketCatalog.findById(file.getBucketAwsId())
                .orElseThrow(() -> new NotFoundException("Bucket no encontrado con id " + file.getBucketAwsId()));
        return new ObjectLocation(bucket.getBucketName(), file.getKey());
    }

    private Duration boundedTtl(Duration ttl) {
        Duration maxTtl = awsProperties.getS3().getPresignedUrlMaxTtl();
        Duration effectiveTtl = ttl != null ? ttl : Duration.ofMinutes(10);
        if (effectiveTtl.compareTo(maxTtl) > 0) {
            throw new IllegalArgumentException("El TTL de la URL prefirmada no puede superar " + maxTtl.toMinutes() + " minutos");
        }
        return effectiveTtl;
    }

    private record ObjectLocation(String bucketName, String objectKey) {
    }

    /** Borrado best-effort usado como compensación: nunca debe ocultar el error original. */
    private void deleteQuietly(String bucket, String key) {
        try {
            storageGateway.delete(bucket, key);
        } catch (RuntimeException e) {
            log.warn("Compensation delete failed for s3://{}/{}: {}", bucket, key, e.getMessage());
        }
    }

    private String buildFileName(MultipartFile file) {
        String original = file.getOriginalFilename();
        String extension = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1)
                : "bin";
        return UUID.randomUUID() + "." + extension;
    }

    private String buildObjectKey(String folder, String fileName) {
        if (folder == null || folder.isBlank()) {
            return fileName;
        }
        String sanitized = folder.endsWith("/") ? folder : folder + "/";
        return sanitized + fileName;
    }

    /** Sink que descarta los bytes (para el cálculo del hash sin cargar el archivo en memoria). */
    private static final class OutputStreamNull extends java.io.OutputStream {
        private static final OutputStreamNull INSTANCE = new OutputStreamNull();

        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b, int off, int len) {
        }
    }
}
