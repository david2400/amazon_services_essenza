package com.essenza.aws.modules.amazonSQS.infrastructure.config;

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
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@RequiredArgsConstructor
public class SqsClientConfig {

    private final AwsProperties awsProperties;

    private AwsCredentialsProvider credentialsProvider() {
        if (StringUtils.hasText(awsProperties.getAccessKeyId()) && StringUtils.hasText(awsProperties.getSecretKey())) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsProperties.getAccessKeyId(), awsProperties.getSecretKey()));
        }
        return DefaultCredentialsProvider.create();
    }

    /**
     * Los timeouts de SQS deben superar el long polling máximo (WaitTimeSeconds = 20s);
     * de lo contrario ReceiveMessage se cortaría antes de que AWS responda.
     */
    private ClientOverrideConfiguration overrideConfiguration() {
        AwsProperties.Sqs sqs = awsProperties.getSqs();
        return ClientOverrideConfiguration.builder()
                .apiCallTimeout(sqs.getApiCallTimeout())
                .apiCallAttemptTimeout(sqs.getApiCallAttemptTimeout())
                .retryPolicy(RetryPolicy.builder()
                        .numRetries(sqs.getMaxRetries())
                        .build())
                .build();
    }

    @Bean(destroyMethod = "close")
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(credentialsProvider())
                .overrideConfiguration(overrideConfiguration())
                .build();
    }

    @Bean(destroyMethod = "close")
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(credentialsProvider())
                .overrideConfiguration(overrideConfiguration())
                .build();
    }
}
