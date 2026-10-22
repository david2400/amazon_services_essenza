package com.essenza.aws.modules.amazonSNS.domain.dto.topics;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class UpdateSnsTopicDto extends AuditInfoDto {
    @Size(max = 256)
    private String displayName;

    private String policy;

    @Schema(description = "Atributos adicionales del tópico")
    private Map<String, String> attributes;

    @Schema(description = "Tags del tópico")
    private Map<String, String> tags;

    private String status;
}
