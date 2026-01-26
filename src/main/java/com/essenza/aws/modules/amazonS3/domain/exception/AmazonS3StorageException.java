package com.essenza.aws.modules.amazonS3.domain.exception;

/**
 * Domain exception thrown when any storage related operation with Amazon S3 fails.
 */
public class AmazonS3StorageException extends RuntimeException {

    public AmazonS3StorageException(String message) {
        super(message);
    }

    public AmazonS3StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
