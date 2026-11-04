package com.essenza.aws.modules.amazonS3.infrastructure.outbound.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class S3ObjectUrlBuilderTest {

    @Test
    void usaElEndpointRegionalHttps() {
        assertThat(S3ObjectUrlBuilder.build("essenza-correos", "us-east-1", "email/imagenes/2026/11/abc.png"))
                .isEqualTo("https://essenza-correos.s3.us-east-1.amazonaws.com/email/imagenes/2026/11/abc.png");
    }

    @Test
    void conservaLasBarrasYCodificaCadaSegmento() {
        assertThat(S3ObjectUrlBuilder.build("b", "sa-east-1", "carpeta con espacios/ñ+ñ/a~b*.png"))
                .isEqualTo("https://b.s3.sa-east-1.amazonaws.com/carpeta%20con%20espacios/%C3%B1%2B%C3%B1/a~b%2A.png");
    }

    @Test
    void bucketsConPuntosUsanPathStyle() {
        assertThat(S3ObjectUrlBuilder.build("img.essenza.co", "us-east-1", "email/imagenes/a.png"))
                .isEqualTo("https://s3.us-east-1.amazonaws.com/img.essenza.co/email/imagenes/a.png");
    }

    @Test
    void regionesDeChinaUsanSuDominio() {
        assertThat(S3ObjectUrlBuilder.build("b", "cn-north-1", "a.png"))
                .isEqualTo("https://b.s3.cn-north-1.amazonaws.com.cn/a.png");
    }
}
