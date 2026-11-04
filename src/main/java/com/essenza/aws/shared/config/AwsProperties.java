package com.essenza.aws.shared.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    private String accessKeyId;

    private String secretKey;

    @NotNull
    private String region = "us-east-1";

    private Sns sns = new Sns();

    private Sqs sqs = new Sqs();

    private S3 s3 = new S3();

    @Data
    public static class Sns {
        @NotNull
        private Duration apiCallTimeout = Duration.ofSeconds(15);

        @NotNull
        private Duration apiCallAttemptTimeout = Duration.ofSeconds(5);

        @Min(0)
        private int maxRetries = 3;
    }

    @Data
    public static class Sqs {
        /** Debe superar el long polling máximo de ReceiveMessage (WaitTimeSeconds = 20s). */
        @NotNull
        private Duration apiCallTimeout = Duration.ofSeconds(30);

        @NotNull
        private Duration apiCallAttemptTimeout = Duration.ofSeconds(25);

        @Min(0)
        private int maxRetries = 3;
    }

    @Data
    public static class S3 {
        /**
         * Prefijos de clave cuyos objetos se sirven públicamente (bucket policy o CloudFront).
         * Los objetos bajo estos prefijos se suben con Cache-Control de larga duración y
         * Content-Disposition: inline. Variable de entorno: AWS_S3_PREFIJOS_PUBLICOS (separados por coma).
         */
        private List<String> prefijosPublicos = new ArrayList<>(List.of("email/imagenes/"));

        /** Cache-Control para objetos públicos. Las claves son UUID y nunca se sobrescriben. */
        @NotNull
        private String cacheControlPublico = "public, max-age=31536000, immutable";
    }
}
