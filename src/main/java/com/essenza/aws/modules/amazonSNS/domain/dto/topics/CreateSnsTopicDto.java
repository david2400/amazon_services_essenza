package com.essenza.aws.modules.amazonSNS.domain.dto.topics;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class CreateSnsTopicDto extends AuditInfoDto {
    @NotBlank
    @Size(max = 256)
    @Schema(description = "Nombre único del tópico", example = "essenza-notifications")
    private String name;

    @Size(max = 256)
    @Schema(description = "Nombre amigable mostrado en la consola", example = "Essenza Notifications")
    private String displayName;

    @Schema(description = "ARN asignado por AWS tras crear el tópico", example = "arn:aws:sns:us-east-1:247455203376:essenza-notifications")
    private String arn;

    @Schema(description = "Política JSON completa del tópico (documento IAM válido). Dejar vacío si no aplica",
            example = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":\"sns:Publish\",\"Resource\":\"*\"}]}")
    private String policy;

    @Schema(description = "Indica si el tópico es FIFO", example = "false")
    private Boolean fifoTopic;

    @Schema(description = "Habilita deduplicación basada en contenido", example = "false")
    private Boolean contentBasedDeduplication;

    @Schema(description = "Atributos adicionales del tópico", example = "{\"TracingConfig\": \"PassThrough\"}")
    private Map<String, String> attributes;

    @Schema(description = "Tags para el tópico", example = "{\"env\": \"dev\", \"project\": \"essenza\"}")
    private Map<String, String> tags;

    @Schema(description = "Estado interno del tópico", example = "ACTIVE")
    private String status;
}
