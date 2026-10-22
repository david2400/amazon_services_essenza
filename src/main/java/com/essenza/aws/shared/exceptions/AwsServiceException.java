package com.essenza.aws.shared.exceptions;

public class AwsServiceException extends RuntimeException {

    private final String awsErrorCode;

    public AwsServiceException(String message, String awsErrorCode, Throwable cause) {
        super(message, cause);
        this.awsErrorCode = awsErrorCode;
    }

    public String getAwsErrorCode() {
        return awsErrorCode;
    }
}
