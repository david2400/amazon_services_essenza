package com.essenza.aws.modules.amazonSQS.domain.dto.messages;

import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueConstraints;
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

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveSqsMessagesDto extends AuditInfoDto {

    @NotBlank
    @Pattern(regexp = SqsQueueConstraints.QUEUE_URL_PATTERN, message = "queueUrl must be a valid SQS queue URL")
    @Schema(example = "https://sqs.us-east-1.amazonaws.com/123456789012/essenza-orders")
    private String queueUrl;

    @Min(1)
    @Max(SqsQueueConstraints.MAX_RECEIVE_MESSAGES)
    @Schema(description = "Máximo de mensajes a recibir (1-10)", example = "10", defaultValue = "1")
    private Integer maxNumberOfMessages;

    @Min(0)
    @Max(SqsQueueConstraints.MAX_WAIT_TIME_SECONDS)
    @Schema(description = "Long polling en segundos (0-20). Si se omite aplica ReceiveMessageWaitTimeSeconds de la cola", example = "10")
    private Integer waitTimeSeconds;

    @Min(0)
    @Max(SqsQueueConstraints.MAX_VISIBILITY_TIMEOUT)
    @Schema(description = "Visibility timeout para los mensajes recibidos (0-43200). Si se omite aplica el de la cola", example = "30")
    private Integer visibilityTimeout;
}
