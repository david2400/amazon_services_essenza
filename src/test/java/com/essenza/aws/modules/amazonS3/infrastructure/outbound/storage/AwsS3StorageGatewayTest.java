package com.essenza.aws.modules.amazonS3.infrastructure.outbound.storage;

import com.essenza.aws.modules.amazonS3.domain.model.S3UploadResult;
import com.essenza.aws.shared.config.AwsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AwsS3StorageGatewayTest {

    private S3Client s3Client;
    private AwsS3StorageGateway gateway;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        AwsProperties properties = new AwsProperties();
        properties.setRegion("us-east-1");
        gateway = new AwsS3StorageGateway(s3Client, mock(S3Presigner.class), properties);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("\"etag-1\"").build());
    }

    @Test
    void imagenDeCorreoSeSubeConCabecerasPublicasYUrlRegional() {
        String key = "email/imagenes/2026/11/0f9e8d7c-uuid.png";

        S3UploadResult result = gateway.upload("essenza-correos", key, "image/png", new ByteArrayInputStream(new byte[]{1, 2}), 2);

        PutObjectRequest request = capturePut();
        assertThat(request.bucket()).isEqualTo("essenza-correos");
        assertThat(request.key()).isEqualTo(key);
        assertThat(request.contentType()).isEqualTo("image/png");
        assertThat(request.cacheControl()).isEqualTo("public, max-age=31536000, immutable");
        assertThat(request.contentDisposition()).isEqualTo("inline");
        assertThat(result.getUrl()).isEqualTo("https://essenza-correos.s3.us-east-1.amazonaws.com/" + key);
        assertThat(result.getKey()).isEqualTo(key);
        assertThat(result.getETag()).isEqualTo("\"etag-1\"");
    }

    @Test
    void archivoFueraDelPrefijoNoLlevaCacheNiDisposition() {
        gateway.upload("documentos", "cedulas/123/uuid.pdf", "application/pdf", new ByteArrayInputStream(new byte[]{1}), 1);

        PutObjectRequest request = capturePut();
        assertThat(request.contentType()).isEqualTo("application/pdf");
        assertThat(request.cacheControl()).isNull();
        assertThat(request.contentDisposition()).isNull();
    }

    @Test
    void contentTypeGenericoSeDeduceDeLaExtension() {
        gateway.upload("essenza-correos", "email/imagenes/2026/11/uuid.gif", "application/octet-stream",
                new ByteArrayInputStream(new byte[]{1}), 1);

        assertThat(capturePut().contentType()).isEqualTo("image/gif");
    }

    private PutObjectRequest capturePut() {
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        return captor.getValue();
    }
}
