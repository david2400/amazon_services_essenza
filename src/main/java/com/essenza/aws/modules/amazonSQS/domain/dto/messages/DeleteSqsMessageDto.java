package com.essenza.aws.modules.amazonSQS.domain.dto.messages;

import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueConstraints;
import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class DeleteSqsMessageDto extends AuditInfoDto {

    @NotBlank
    @Pattern(regexp = SqsQueueConstraints.QUEUE_URL_PATTERN, message = "queueUrl must be a valid SQS queue URL")
    private String queueUrl;

    @NotBlank
    @Schema(description = "receiptHandle obtenido al recibir el mensaje")
    private String receiptHandle;

    @Schema(description = "messageId del mensaje (opcional, solo para el registro de auditoría)")
    private String messageId;
}
