package com.essenza.aws.modules.amazonS3.infrastructure.outbound.storage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Construye la URL https permanente de un objeto usando el endpoint <b>regional</b> de S3:
 * {@code https://<bucket>.s3.<region>.amazonaws.com/<key>}.
 *
 * <p>Se construye a mano (en vez de {@code S3Utilities#getUrl}) porque el SDK 2.17 devuelve el endpoint
 * global {@code s3.amazonaws.com} para us-east-1. Los buckets con puntos usan path-style
 * ({@code https://s3.<region>.amazonaws.com/<bucket>/<key>}) porque el certificado TLS comodín de S3 no
 * cubre subdominios con puntos. Cada segmento de la clave se codifica según RFC 3986 y se conservan los {@code /}.
 */
public final class S3ObjectUrlBuilder {

    private S3ObjectUrlBuilder() {
    }

    public static String build(String bucket, String region, String key) {
        String domain = region.startsWith("cn-") ? "amazonaws.com.cn" : "amazonaws.com";
        String encodedKey = encodeKey(key);
        if (bucket.contains(".")) {
            return "https://s3." + region + "." + domain + "/" + bucket + "/" + encodedKey;
        }
        return "https://" + bucket + ".s3." + region + "." + domain + "/" + encodedKey;
    }

    static String encodeKey(String key) {
        return Arrays.stream(key.split("/", -1))
                .map(S3ObjectUrlBuilder::encodeSegment)
                .collect(Collectors.joining("/"));
    }

    private static String encodeSegment(String segment) {
        return URLEncoder.encode(segment, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }
}
