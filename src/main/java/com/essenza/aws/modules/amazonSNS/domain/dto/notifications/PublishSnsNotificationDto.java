package com.essenza.aws.modules.amazonSNS.domain.dto.notifications;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class PublishSnsNotificationDto extends AuditInfoDto {

    public static final String SNS_TOPIC_ARN_PATTERN = "^arn:aws[a-zA-Z-]*:sns:[a-z0-9-]+:\\d{12}:.+$";

    @NotBlank
    @Pattern(regexp = SNS_TOPIC_ARN_PATTERN, message = "topicArn must be a valid SNS topic ARN")
    private String topicArn;

    @Size(max = 100)
    @Schema(description = "Asunto del mensaje (máx. 100 caracteres, límite de SNS)", example = "Nueva orden creada")
    private String subject;

    @NotBlank
    private String message;

    @Schema(description = "Atributos de mensaje (String)")
    private Map<String, String> messageAttributes;

    @Schema(description = "Requerido solo para tópicos FIFO")
    private String messageGroupId;

    @Schema(description = "ID de deduplicación para tópicos FIFO sin deduplicación por contenido")
    private String messageDeduplicationId;
}
