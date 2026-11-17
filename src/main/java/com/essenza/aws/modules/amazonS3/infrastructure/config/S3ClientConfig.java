package com.essenza.aws.modules.amazonS3.infrastructure.config;

import com.essenza.aws.shared.config.AwsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class S3ClientConfig {

    private final AwsProperties awsProperties;

    private AwsCredentialsProvider credentialsProvider() {
        if (StringUtils.hasText(awsProperties.getAccessKeyId()) && StringUtils.hasText(awsProperties.getSecretKey())) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsProperties.getAccessKeyId(), awsProperties.getSecretKey()));
        }
        return DefaultCredentialsProvider.create();
    }

    private ClientOverrideConfiguration overrideConfiguration() {
        AwsProperties.S3 s3 = awsProperties.getS3();
        return ClientOverrideConfiguration.builder()
                .apiCallTimeout(s3.getApiCallTimeout())
                .apiCallAttemptTimeout(s3.getApiCallAttemptTimeout())
                .retryPolicy(RetryPolicy.builder()
                        .numRetries(s3.getMaxRetries())
                        .build())
                .build();
    }

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(credentialsProvider())
                .build();
    }

    @Bean(destroyMethod = "close")
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(credentialsProvider())
                .overrideConfiguration(overrideConfiguration())
                .build();
    }
}
