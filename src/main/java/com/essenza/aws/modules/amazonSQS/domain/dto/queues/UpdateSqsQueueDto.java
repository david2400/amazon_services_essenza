package com.essenza.aws.modules.amazonSQS.domain.dto.queues;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSqsQueueDto extends AuditInfoDto {
    @Min(0)
    @Max(SqsQueueConstraints.MAX_DELAY_SECONDS)
    private Integer delaySeconds;

    @Min(SqsQueueConstraints.MIN_MESSAGE_SIZE)
    @Max(SqsQueueConstraints.MAX_MESSAGE_SIZE)
    private Integer maximumMessageSize;

    @Min(SqsQueueConstraints.MIN_RETENTION_PERIOD)
    @Max(SqsQueueConstraints.MAX_RETENTION_PERIOD)
    private Integer messageRetentionPeriod;

    @Min(0)
    @Max(SqsQueueConstraints.MAX_VISIBILITY_TIMEOUT)
    private Integer visibilityTimeout;

    @Schema(description = "RedrivePolicy JSON con deadLetterTargetArn y maxReceiveCount")
    private String redrivePolicy;

    @Schema(description = "Atributos adicionales de la cola. FifoQueue y ContentBasedDeduplication no son actualizables")
    private Map<String, String> attributes;

    @Schema(description = "Tags a agregar o sobrescribir en la cola")
    private Map<String, String> tags;

    private String status;
}
