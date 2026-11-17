package com.essenza.aws.modules.amazonS3.infrastructure.inbound.rest;

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
 * Red de seguridad para excepciones del SDK de S3 que no hayan sido traducidas por
 * {@code AwsS3StorageGateway} (que las convierte en {@code AwsServiceException} → 502 o
 * {@code NotFoundException} → 404). Tiene prioridad sobre el {@code GlobalExceptionHandler}
 * y nunca expone trazas ni mensajes internos del SDK.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.essenza.aws.modules.amazonS3")
public class AmazonS3ExceptionHandler {

    @ExceptionHandler(NoSuchKeyException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchKey(NoSuchKeyException ex) {
        return build(HttpStatus.NOT_FOUND, "El archivo no existe en Amazon S3");
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
