package com.essenza.aws.modules.amazonS3.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonS3.domain.exception.AmazonS3StorageException;
import com.essenza.aws.modules.amazonS3.domain.exception.ResourceNotFoundException;
import com.essenza.aws.shared.exceptions.GlobalExceptionHandler.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.Instant;

/**
 * Traduce las excepciones del módulo S3 a respuestas HTTP. Tiene prioridad sobre el
 * {@code GlobalExceptionHandler} (que responde 500 genérico a todo lo que no conoce) y solo aplica
 * a los controllers de este módulo. Nunca expone trazas ni mensajes internos del SDK.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.essenza.aws.modules.amazonS3")
public class AmazonS3ExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NoSuchKeyException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchKey(NoSuchKeyException ex) {
        return build(HttpStatus.NOT_FOUND, "El archivo no existe en Amazon S3");
    }

    @ExceptionHandler(AmazonS3StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(AmazonS3StorageException ex) {
        log.error("Error de Amazon S3: {}", ex.getMessage(), ex);
        return build(HttpStatus.BAD_GATEWAY, "Error al comunicarse con Amazon S3: " + ex.getMessage());
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ErrorResponse> handleS3(S3Exception ex) {
        String code = ex.awsErrorDetails() != null ? ex.awsErrorDetails().errorCode() : "unknown";
        log.error("Error de Amazon S3 [{}]: {}", code, ex.getMessage(), ex);
        return build(HttpStatus.BAD_GATEWAY, "Error al comunicarse con Amazon S3 (" + code + ")");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message));
    }
}
