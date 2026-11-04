package com.essenza.aws.modules.amazonS3.infrastructure.outbound.storage;

import com.essenza.aws.shared.config.AwsProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class S3ObjectHeadersPolicyTest {

    private final S3ObjectHeadersPolicy policy = new S3ObjectHeadersPolicy(new AwsProperties.S3());

    @Test
    void clavesBajoPrefijoPublicoLlevanCacheLargoEInline() {
        String key = "email/imagenes/2026/11/0b1c2d3e-uuid.png";

        assertThat(policy.isPublic(key)).isTrue();
        assertThat(policy.cacheControlFor(key)).isEqualTo("public, max-age=31536000, immutable");
        assertThat(policy.contentDispositionFor(key)).isEqualTo("inline");
    }

    @Test
    void clavesFueraDelPrefijoNoCambian() {
        for (String key : List.of("documentos/123/uuid.pdf", "uuid.png", "email/otros/uuid.png", "x/email/imagenes/uuid.png")) {
            assertThat(policy.isPublic(key)).as(key).isFalse();
            assertThat(policy.cacheControlFor(key)).as(key).isNull();
            assertThat(policy.contentDispositionFor(key)).as(key).isNull();
        }
    }

    @Test
    void prefijoSinBarraFinalNoCoincideConCarpetasHermanas() {
        AwsProperties.S3 props = new AwsProperties.S3();
        props.setPrefijosPublicos(List.of("email/imagenes"));
        S3ObjectHeadersPolicy custom = new S3ObjectHeadersPolicy(props);

        assertThat(custom.isPublic("email/imagenes/a.png")).isTrue();
        assertThat(custom.isPublic("email/imagenes-privadas/a.png")).isFalse();
    }

    @Test
    void conservaElContentTypeDeclarado() {
        assertThat(policy.resolveContentType("email/imagenes/a.png", "image/png")).isEqualTo("image/png");
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "a.png, NULL, image/png",
            "a.jpg, '', image/jpeg",
            "a.jpeg, application/octet-stream, image/jpeg",
            "a.gif, APPLICATION/OCTET-STREAM, image/gif",
            "a.pdf, NULL, application/pdf",
            "sin-extension, NULL, application/octet-stream"
    })
    void deduceElContentTypeDeLaExtension(String key, String declared, String expected) {
        assertThat(policy.resolveContentType(key, declared)).isEqualTo(expected);
    }
}
