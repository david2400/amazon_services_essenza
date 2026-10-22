package com.essenza.aws.shared.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Data
@Validated
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    private String accessKeyId;

    private String secretKey;

    @NotNull
    private String region = "us-east-1";

    private Sns sns = new Sns();

    @Data
    public static class Sns {
        @NotNull
        private Duration apiCallTimeout = Duration.ofSeconds(15);

        @NotNull
        private Duration apiCallAttemptTimeout = Duration.ofSeconds(5);

        @Min(0)
        private int maxRetries = 3;
    }
}
