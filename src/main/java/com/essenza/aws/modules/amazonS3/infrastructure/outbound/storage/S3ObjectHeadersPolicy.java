package com.essenza.aws.modules.amazonS3.infrastructure.outbound.storage;

import com.essenza.aws.shared.config.AwsProperties;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Decide las cabeceras HTTP que S3 guardará con cada objeto subido.
 *
 * <ul>
 *   <li>Content-Type: el recibido; si viene vacío o como application/octet-stream se deduce de la extensión.</li>
 *   <li>Cache-Control y Content-Disposition: inline solo para claves bajo {@code aws.s3.prefijos-publicos}
 *       (p. ej. {@code email/imagenes/}), que se sirven públicamente y nunca se sobrescriben.</li>
 * </ul>
 */
public class S3ObjectHeadersPolicy {

    static final String DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_OCTET_STREAM_VALUE;
    static final String INLINE = "inline";

    private final List<String> publicPrefixes;
    private final String publicCacheControl;

    public S3ObjectHeadersPolicy(AwsProperties.S3 properties) {
        this.publicPrefixes = properties.getPrefijosPublicos() == null ? List.of()
                : properties.getPrefijosPublicos().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(prefix -> prefix.startsWith("/") ? prefix.substring(1) : prefix)
                .map(prefix -> prefix.endsWith("/") ? prefix : prefix + "/")
                .toList();
        this.publicCacheControl = properties.getCacheControlPublico();
    }

    public String resolveContentType(String key, String declaredContentType) {
        if (StringUtils.hasText(declaredContentType)
                && !DEFAULT_CONTENT_TYPE.equalsIgnoreCase(declaredContentType.trim())) {
            return declaredContentType.trim();
        }
        return MediaTypeFactory.getMediaType(key)
                .map(MediaType::toString)
                .orElse(DEFAULT_CONTENT_TYPE);
    }

    public boolean isPublic(String key) {
        return key != null && publicPrefixes.stream().anyMatch(key::startsWith);
    }

    /** Cache-Control para la clave, o {@code null} si no aplica (el objeto conserva el comportamiento por defecto). */
    public String cacheControlFor(String key) {
        return isPublic(key) ? publicCacheControl : null;
    }

    /** Content-Disposition para la clave, o {@code null} si no aplica. */
    public String contentDispositionFor(String key) {
        return isPublic(key) ? INLINE : null;
    }
}
