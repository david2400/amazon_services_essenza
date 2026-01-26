package com.essenza.aws.modules.amazonS3.application.services;

import com.essenza.aws.modules.amazonS3.application.input.files_amazon.CreatePresignedGetUrlUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.DeleteFileByKeyAndBucketUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.DownloadFileUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByBucketAwsIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByDocumentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByParentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.ReplaceFileUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UpdateDocumentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UpdateFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UploadFileUseCase;
import com.essenza.aws.modules.amazonS3.application.output.storage.S3StorageGateway;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.UpdateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.exception.AmazonS3StorageException;
import com.essenza.aws.modules.amazonS3.domain.model.S3ObjectStream;
import com.essenza.aws.modules.amazonS3.domain.model.S3UploadResult;
import com.essenza.aws.modules.amazonS3.domain.exception.ResourceNotFoundException;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers.FilesAmazonS3Mapper;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.BuketAwsEntity;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.FilesAmazonS3Entity;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.buketaws.JpaBuketAwsRepository;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.files_amazon.FilesAmazonS3RepositoryAdapter;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.files_amazon.JpaFilesAmazonS3Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class FilesAmazonS3ServiceImpl implements
        UploadFileUseCase,
        DownloadFileUseCase,
        CreatePresignedGetUrlUseCase,
        ReplaceFileUseCase,
        UpdateDocumentIdUseCase,
        DeleteFileByKeyAndBucketUseCase,
        UpdateFilesAmazonS3UseCase,
        FindFilesAmazonS3UseCase,
        FindFilesAmazonS3ByDocumentIdUseCase,
        FindFilesAmazonS3ByBucketAwsIdUseCase,
        FindFilesAmazonS3ByParentIdUseCase {

    private final FilesAmazonS3RepositoryAdapter repository;
    private final JpaBuketAwsRepository buketAwsRepository;
    private final JpaFilesAmazonS3Repository jpaFilesRepository;
    private final FilesAmazonS3Mapper mapper;
    private final S3StorageGateway storageGateway;

//    public FilesAmazonS3ServiceImpl(
//            FilesAmazonS3RepositoryAdapter repository,
//            JpaBuketAwsRepository buketAwsRepository,
//            JpaFilesAmazonS3Repository jpaFilesRepository,
//            FilesAmazonS3Mapper mapper,
//            S3StorageGateway storageGateway
//    ) {
//        this.repository = repository;
//        this.buketAwsRepository = buketAwsRepository;
//        this.jpaFilesRepository = jpaFilesRepository;
//        this.mapper = mapper;
//        this.storageGateway = storageGateway;
//    }

    public FilesAmazonS3Dto create(CreateFilesAmazonS3Dto input) {
        return repository.create(input);
    }

    public FilesAmazonS3Dto update(Long id, UpdateFilesAmazonS3Dto input) {
        return repository.update(id, input);
    }

    public boolean deleteById(Long id) {
        return repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<FilesAmazonS3Dto> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<FilesAmazonS3Dto> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FilesAmazonS3Dto> findByDocumentId(Integer documentId) {
        return repository.findByDocumentId(documentId);
    }

    @Transactional(readOnly = true)
    public List<FilesAmazonS3Dto> findByBucketAwsId(Long bucketAwsId) {
        return repository.findByBucketAwsId(bucketAwsId);
    }

    @Transactional(readOnly = true)
    public List<FilesAmazonS3Dto> findByParentId(Long parentId) {
        return repository.findByParentId(parentId);
    }

    // --- S3 logic methods (Use cases) ---

    @Override
    public FilesAmazonS3Dto upload(MultipartFile file,
                                   String bucket,
                                   String folder,
                                   CreateFilesAmazonS3Dto meta,
                                   String userId) {
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

            meta.setFileName(fileName);
            meta.setKey(key);
            meta.setETag(uploadResult.getETag());
            meta.setS3Url(uploadResult.getUrl());

            return repository.create(meta);
        } catch (IOException e) {
            throw new AmazonS3StorageException("Error uploading file to S3", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public S3ObjectStream download(String bucket, String key) {
        return storageGateway.download(bucket, key);
    }

    @Override
    @Transactional(readOnly = true)
    public String createPresignedGetUrl(String bucket, String key, Duration ttl) {
        Duration effectiveTtl = ttl != null ? ttl : Duration.ofMinutes(10);
        return storageGateway.createPresignedGetUrl(bucket, key, effectiveTtl);
    }

    @Override
    public FilesAmazonS3Dto replace(Long id, MultipartFile file) {
        FilesAmazonS3Dto current = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo con id " + id + " no existe"));

        BuketAwsEntity bucket = buketAwsRepository.findById(current.getBucketAwsId())
                .orElseThrow(() -> new ResourceNotFoundException("Bucket no encontrado con id " + current.getBucketAwsId()));

        String bucketName = bucket.getBucketName();
        String folder = bucket.getBucketFolder() != null ? bucket.getBucketFolder() : "";

        if (current.getKey() != null) {
            storageGateway.delete(bucketName, current.getKey());
        }

        try (InputStream content = file.getInputStream()) {
            String newName = buildFileName(file);
            String newKey = buildObjectKey(folder, newName);

            S3UploadResult uploadResult = storageGateway.upload(
                    bucketName,
                    newKey,
                    file.getContentType(),
                    content,
                    file.getSize()
            );

            UpdateFilesAmazonS3Dto upd = new UpdateFilesAmazonS3Dto();
            upd.setFileName(newName);
            upd.setKey(newKey);
            upd.setETag(uploadResult.getETag());
            upd.setS3Url(uploadResult.getUrl());

            return repository.update(id, upd);
        } catch (IOException e) {
            throw new AmazonS3StorageException("Error replacing file in S3", e);
        }
    }

    @Override
    public FilesAmazonS3Dto updateDocumentId(Long id, Integer documentId, String userId) {
        UpdateFilesAmazonS3Dto upd = new UpdateFilesAmazonS3Dto();
        upd.setDocumentId(documentId);
        return repository.update(id, upd);
    }

    @Override
    public List<FilesAmazonS3Dto> deleteByKeyAndBucket(String key, Long bucketAwsId, String userId) {
        List<FilesAmazonS3Entity> matches = jpaFilesRepository.findByKeyAndBucketAwsId(key, bucketAwsId);
        if (matches.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró archivo con key " + key + " y bucketAwsId " + bucketAwsId);
        }
        if (matches.size() > 1) {
            throw new RuntimeException("Multiple files found for key and bucketAwsId: " + matches.size());
        }

        FilesAmazonS3Entity entity = matches.get(0);
        BuketAwsEntity bucket = buketAwsRepository.findById(bucketAwsId)
                .orElseThrow(() -> new ResourceNotFoundException("Bucket no encontrado con id " + bucketAwsId));
        storageGateway.delete(bucket.getBucketName(), key);

        // Optionally mark deleted in DB here if you have soft delete
        // For now, just return the matched record mapped to DTO
        return List.of(mapper.toDto(entity));
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
}
