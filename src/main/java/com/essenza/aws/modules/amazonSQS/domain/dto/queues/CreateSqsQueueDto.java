package com.essenza.aws.modules.amazonSQS.domain.dto.queues;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSqsQueueDto extends AuditInfoDto {
    @NotBlank
    @Pattern(regexp = SqsQueueConstraints.QUEUE_NAME_PATTERN,
            message = "name must be 1-80 characters (letters, digits, '-' or '_'), optionally ending in .fifo")
    @Schema(description = "Nombre único de la cola. Las colas FIFO deben terminar en .fifo", example = "essenza-orders")
    private String name;

    @Schema(description = "URL asignada por AWS tras crear la cola", accessMode = Schema.AccessMode.READ_ONLY,
            example = "https://sqs.us-east-1.amazonaws.com/123456789012/essenza-orders")
    private String queueUrl;

    @Schema(description = "ARN asignado por AWS tras crear la cola", accessMode = Schema.AccessMode.READ_ONLY,
            example = "arn:aws:sqs:us-east-1:123456789012:essenza-orders")
    private String arn;

    @Min(0)
    @Max(SqsQueueConstraints.MAX_DELAY_SECONDS)
    @Schema(description = "Retraso de entrega en segundos (0-900)", example = "0")
    private Integer delaySeconds;

    @Min(SqsQueueConstraints.MIN_MESSAGE_SIZE)
    @Max(SqsQueueConstraints.MAX_MESSAGE_SIZE)
    @Schema(description = "Tamaño máximo del mensaje en bytes (1024-1048576)", example = "262144")
    private Integer maximumMessageSize;

    @Min(SqsQueueConstraints.MIN_RETENTION_PERIOD)
    @Max(SqsQueueConstraints.MAX_RETENTION_PERIOD)
    @Schema(description = "Retención de mensajes en segundos (60-1209600)", example = "345600")
    private Integer messageRetentionPeriod;

    @Min(0)
    @Max(SqsQueueConstraints.MAX_VISIBILITY_TIMEOUT)
    @Schema(description = "Visibility timeout en segundos (0-43200)", example = "30")
    private Integer visibilityTimeout;

    @Schema(description = "Indica si la cola es FIFO (el nombre debe terminar en .fifo). No actualizable", example = "false")
    private Boolean fifoQueue;

    @Schema(description = "Deduplicación basada en contenido (solo colas FIFO). No actualizable", example = "false")
    private Boolean contentBasedDeduplication;

    @Schema(description = "RedrivePolicy JSON con deadLetterTargetArn y maxReceiveCount",
            example = "{\"deadLetterTargetArn\":\"arn:aws:sqs:us-east-1:123456789012:essenza-orders-dlq\",\"maxReceiveCount\":\"5\"}")
    private String redrivePolicy;

    @Schema(description = "Atributos adicionales de la cola (nombres de atributo de AWS)",
            example = "{\"ReceiveMessageWaitTimeSeconds\": \"10\"}")
    private Map<String, String> attributes;

    @Schema(description = "Tags de la cola", example = "{\"env\": \"dev\", \"project\": \"essenza\"}")
    private Map<String, String> tags;

    @Schema(description = "Estado interno de la cola", example = "ACTIVE")
    private String status;
}
