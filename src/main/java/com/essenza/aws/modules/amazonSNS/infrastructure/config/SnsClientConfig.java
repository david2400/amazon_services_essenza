package com.essenza.aws.modules.amazonSNS.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class SnsClientConfig {

    @Value("${aws.access.key.id:}")
    private String awsAccessKeyId;

    @Value("${aws.secret.key:}")
    private String awsSecretKey;

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    private AwsCredentialsProvider credentialsProvider() {
        if (StringUtils.hasText(awsAccessKeyId) && StringUtils.hasText(awsSecretKey)) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKeyId, awsSecretKey));
        }
        return DefaultCredentialsProvider.create();
    }

    @Bean(destroyMethod = "close")
    public SnsClient snsClient() {
        return SnsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider())
                .build();
    }
}
