package com.essenza.aws.modules.amazonS3.domain.exception;

/**
 * Exception thrown when an expected resource (file, bucket, etc.) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
