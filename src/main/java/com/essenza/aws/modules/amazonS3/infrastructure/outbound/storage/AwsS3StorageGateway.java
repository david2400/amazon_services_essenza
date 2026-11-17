package com.essenza.aws.modules.amazonS3.infrastructure.outbound.storage;

import com.essenza.aws.modules.amazonS3.application.output.storage.S3StorageGateway;
import com.essenza.aws.modules.amazonS3.domain.model.S3ObjectStream;
import com.essenza.aws.modules.amazonS3.domain.model.S3UploadResult;
import com.essenza.aws.shared.config.AwsProperties;
import com.essenza.aws.shared.exceptions.AwsServiceException;
import com.essenza.aws.shared.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class AwsS3StorageGateway implements S3StorageGateway {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsProperties awsProperties;
    private final S3ObjectHeadersPolicy headersPolicy;

    public AwsS3StorageGateway(S3Client s3Client, S3Presigner s3Presigner, AwsProperties awsProperties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.awsProperties = awsProperties;
        this.headersPolicy = new S3ObjectHeadersPolicy(awsProperties.getS3());
    }

    @Override
    public S3UploadResult upload(String bucket,
                                 String key,
                                 String contentType,
                                 InputStream content,
                                 long contentLength) {
        try {
            PutObjectRequest.Builder request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(headersPolicy.resolveContentType(key, contentType));

            // Solo para prefijos públicos (p. ej. email/imagenes/): el resto de objetos no cambia.
            String cacheControl = headersPolicy.cacheControlFor(key);
            if (cacheControl != null) {
                request.cacheControl(cacheControl);
            }
            String contentDisposition = headersPolicy.contentDispositionFor(key);
            if (contentDisposition != null) {
                request.contentDisposition(contentDisposition);
            }

            var response = s3Client.putObject(request.build(), RequestBody.fromInputStream(content, contentLength));

            return S3UploadResult.builder()
                    .key(key)
                    .eTag(response.eTag())
                    .url(S3ObjectUrlBuilder.build(bucket, awsProperties.getRegion(), key))
                    .build();
        } catch (Exception e) {
            throw translate("Failed to upload file to S3", e);
        }
    }

    @Override
    public S3ObjectStream download(String bucket, String key) {
        try {
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
        } catch (NoSuchKeyException e) {
            throw new NotFoundException("El archivo no existe en Amazon S3: " + key);
        } catch (Exception e) {
            throw translate("Failed to download file from S3", e);
        }
    }

    @Override
    public void delete(String bucket, String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            throw translate("Failed to delete object from S3", e);
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
            throw translate("Failed to create presigned URL", e);
        }
    }

    /** Traduce errores del SDK a {@link AwsServiceException} conservando el código de error de AWS. */
    private AwsServiceException translate(String message, Exception e) {
        if (e instanceof S3Exception s3Exception && s3Exception.awsErrorDetails() != null) {
            String code = s3Exception.awsErrorDetails().errorCode();
            log.error("S3 error [{}]: {}", code, s3Exception.awsErrorDetails().errorMessage());
            return new AwsServiceException(message, code, e);
        }
        log.error("S3 error: {}", e.getMessage(), e);
        return new AwsServiceException(message, "S3_ERROR", e);
    }
}
