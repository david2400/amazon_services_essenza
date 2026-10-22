package com.essenza.aws.modules.amazonSNS.infrastructure.config;

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
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
@RequiredArgsConstructor
public class SnsClientConfig {

    private final AwsProperties awsProperties;

    private AwsCredentialsProvider credentialsProvider() {
        if (StringUtils.hasText(awsProperties.getAccessKeyId()) && StringUtils.hasText(awsProperties.getSecretKey())) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsProperties.getAccessKeyId(), awsProperties.getSecretKey()));
        }
        return DefaultCredentialsProvider.create();
    }

    private ClientOverrideConfiguration overrideConfiguration() {
        AwsProperties.Sns sns = awsProperties.getSns();
        return ClientOverrideConfiguration.builder()
                .apiCallTimeout(sns.getApiCallTimeout())
                .apiCallAttemptTimeout(sns.getApiCallAttemptTimeout())
                .retryPolicy(RetryPolicy.builder()
                        .numRetries(sns.getMaxRetries())
                        .build())
                .build();
    }

    @Bean(destroyMethod = "close")
    public SnsClient snsClient() {
        return SnsClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(credentialsProvider())
                .overrideConfiguration(overrideConfiguration())
                .build();
    }

    @Bean(destroyMethod = "close")
    public SnsAsyncClient snsAsyncClient() {
        return SnsAsyncClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(credentialsProvider())
                .overrideConfiguration(overrideConfiguration())
                .build();
    }
}
