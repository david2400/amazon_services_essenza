package com.essenza.aws.modules.amazonS3.application.services;

import com.essenza.aws.modules.amazonS3.application.input.files_amazon.CreatePresignedGetUrlUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.DeleteFileByKeyAndBucketUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.DownloadFileUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByDocumentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByRelationTypeIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByBucketAwsIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.FindFilesAmazonS3ByParentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.ReplaceFileUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UpdateDocumentIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UpdateFilesAmazonS3UseCase;
import com.essenza.aws.modules.amazonS3.application.input.files_amazon.UploadFileUseCase;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.CreateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.FilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.domain.dto.files_amazons3.UpdateFilesAmazonS3Dto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers.FilesAmazonS3Mapper;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.BuketAwsEntity;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.FilesAmazonS3Entity;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.buketaws.JpaBuketAwsRepository;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.files_amazon.FilesAmazonS3RepositoryAdapter;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.files_amazon.JpaFilesAmazonS3Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        FindFilesAmazonS3ByRelationTypeIdUseCase,
        FindFilesAmazonS3ByBucketAwsIdUseCase,
        FindFilesAmazonS3ByParentIdUseCase {

    private final FilesAmazonS3RepositoryAdapter repository;
    private final JpaBuketAwsRepository buketAwsRepository;
    private final JpaFilesAmazonS3Repository jpaFilesRepository;
    private final FilesAmazonS3Mapper mapper;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public FilesAmazonS3ServiceImpl(
            FilesAmazonS3RepositoryAdapter repository,
            JpaBuketAwsRepository buketAwsRepository,
            JpaFilesAmazonS3Repository jpaFilesRepository,
            FilesAmazonS3Mapper mapper,
            S3Client s3Client,
            S3Presigner s3Presigner
    ) {
        this.repository = repository;
        this.buketAwsRepository = buketAwsRepository;
        this.jpaFilesRepository = jpaFilesRepository;
        this.mapper = mapper;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

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
    public List<FilesAmazonS3Dto> findByRelationTypeId(Integer relationTypeId) {
        return repository.findByRelationTypeId(relationTypeId);
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
        try {
            String original = file.getOriginalFilename();
            String ext = original != null && original.contains(".")
                    ? original.substring(original.lastIndexOf('.') + 1)
                    : "bin";
            String basename = UUID.randomUUID().toString();
            String fileName = basename + "." + ext;
            String key = (folder != null ? folder : "") + fileName;

            PutObjectResponse put = s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            URL url = s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build());

            meta.setFileName(fileName);
            meta.setKey(key);
            meta.setETag(put.eTag());
            meta.setS3Url(url.toString());

            return repository.create(meta);
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to S3: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] download(String bucket, String key) {
        ResponseBytes<?> bytes = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucket).key(key).build(),
                ResponseTransformer.toBytes()
        );
        return bytes.asByteArray();
    }

    @Override
    @Transactional(readOnly = true)
    public String createPresignedGetUrl(String bucket, String key, Duration ttl) {
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(ttl != null ? ttl : Duration.ofMinutes(10))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .responseContentDisposition("inline")
                                .build())
                        .build()
        );
        return presigned.url().toExternalForm();
    }

    @Override
    public FilesAmazonS3Dto replace(Long id, MultipartFile file) {
        FilesAmazonS3Dto current = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("File record not found"));

        BuketAwsEntity bucket = buketAwsRepository.findById(current.getBucketAwsId())
                .orElseThrow(() -> new RuntimeException("Bucket not found for id " + current.getBucketAwsId()));

        String bucketName = bucket.getBucketName();
        String folder = bucket.getBucketFolder() != null ? bucket.getBucketFolder() : "";

        // delete old object
        if (current.getKey() != null) {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(current.getKey()).build());
        }

        // new key and file name
        try {
            String original = file.getOriginalFilename();
            String ext = original != null && original.contains(".")
                    ? original.substring(original.lastIndexOf('.') + 1)
                    : "bin";
            String newName = UUID.randomUUID() + "." + ext;
            String newKey = folder + newName;

            PutObjectResponse put = s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucketName).key(newKey).contentType(file.getContentType()).build(),
                    RequestBody.fromBytes(file.getBytes())
            );
            URL url = s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName).key(newKey).build());

            UpdateFilesAmazonS3Dto upd = new UpdateFilesAmazonS3Dto();
            upd.setFileName(newName);
            upd.setKey(newKey);
            upd.setETag(put.getClass() != null ? put.eTag() : null);
            upd.setS3Url(url.toString());

            return repository.update(id, upd);
        } catch (Exception e) {
            throw new RuntimeException("Error replacing file in S3: " + e.getMessage(), e);
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
            throw new RuntimeException("No file found for key and bucketAwsId");
        }
        if (matches.size() > 1) {
            throw new RuntimeException("Multiple files found for key and bucketAwsId: " + matches.size());
        }

        FilesAmazonS3Entity entity = matches.get(0);
        BuketAwsEntity bucket = buketAwsRepository.findById(bucketAwsId)
                .orElseThrow(() -> new RuntimeException("Bucket not found for id " + bucketAwsId));
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket.getBucketName()).key(key).build());

        // Optionally mark deleted in DB here if you have soft delete
        // For now, just return the matched record mapped to DTO
        return List.of(mapper.toDto(entity));
    }
}
