package com.essenza.aws.modules.amazonS3.infrastructure.outbound.storage;

import com.essenza.aws.modules.amazonS3.application.output.storage.S3StorageGateway;
import com.essenza.aws.modules.amazonS3.domain.exception.AmazonS3StorageException;
import com.essenza.aws.modules.amazonS3.domain.model.S3ObjectStream;
import com.essenza.aws.modules.amazonS3.domain.model.S3UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AwsS3StorageGateway implements S3StorageGateway {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

//    public AwsS3StorageGateway(S3Client s3Client, S3Presigner s3Presigner) {
//        this.s3Client = s3Client;
//        this.s3Presigner = s3Presigner;
//    }

    @Override
    public S3UploadResult upload(String bucket,
                                 String key,
                                 String contentType,
                                 InputStream content,
                                 long contentLength) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(Optional.ofNullable(contentType).orElse(DEFAULT_CONTENT_TYPE))
                    .build();

            var response = s3Client.putObject(request, RequestBody.fromInputStream(content, contentLength));

            URL url = s3Client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());

            return S3UploadResult.builder()
                    .key(key)
                    .eTag(response.eTag())
                    .url(url.toString())
                    .build();
        } catch (Exception e) {
            throw new AmazonS3StorageException("Failed to upload file to S3", e);
        }
    }

    @Override
    public S3ObjectStream download(String bucket, String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
        GetObjectResponse metadata = response.response();

        return S3ObjectStream.builder()
                .inputStream(response)
                .contentLength(metadata.contentLength())
                .contentType(metadata.contentType())
                .metadata(metadata.metadata() == null ? Map.of() : Map.copyOf(metadata.metadata()))
                .build();
    }

    @Override
    public void delete(String bucket, String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            throw new AmazonS3StorageException("Failed to delete object from S3", e);
        }
    }

    @Override
    public String createPresignedGetUrl(String bucket, String key, Duration ttl) {
        try {
            return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                            .signatureDuration(ttl)
                            .getObjectRequest(GetObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(key)
                                    .build())
                            .build())
                    .url()
                    .toExternalForm();
        } catch (Exception e) {
            throw new AmazonS3StorageException("Failed to create presigned URL", e);
        }
    }
}
