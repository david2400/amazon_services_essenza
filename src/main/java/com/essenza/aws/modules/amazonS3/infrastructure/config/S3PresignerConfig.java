package com.essenza.aws.modules.amazonS3.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3PresignerConfig {


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
    public S3Presigner S3Presigner() {

        return S3Presigner.builder().region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider())
                .build();

    }

    @Bean(destroyMethod = "close")
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider())
                .build();
    }

}
