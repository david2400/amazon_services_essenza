package com.essenza.aws.modules.amazonSQS.domain.dto.messages;

import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueConstraints;
import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SendSqsMessageDto extends AuditInfoDto {

    @NotBlank
    @Pattern(regexp = SqsQueueConstraints.QUEUE_URL_PATTERN, message = "queueUrl must be a valid SQS queue URL")
    @Schema(example = "https://sqs.us-east-1.amazonaws.com/123456789012/essenza-orders")
    private String queueUrl;

    @NotBlank
    @Size(max = SqsQueueConstraints.MAX_MESSAGE_SIZE)
    @Schema(description = "Cuerpo del mensaje", example = "{\"orderId\": 123}")
    private String messageBody;

    @Min(0)
    @Max(SqsQueueConstraints.MAX_DELAY_SECONDS)
    @Schema(description = "Retraso por mensaje en segundos (0-900). No soportado en colas FIFO")
    private Integer delaySeconds;

    @Schema(description = "Atributos de mensaje (String)")
    private Map<String, String> messageAttributes;

    @Size(max = 128)
    @Schema(description = "Requerido solo para colas FIFO")
    private String messageGroupId;

    @Size(max = 128)
    @Schema(description = "ID de deduplicación para colas FIFO sin deduplicación por contenido")
    private String messageDeduplicationId;
}
