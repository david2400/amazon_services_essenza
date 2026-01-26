package com.essenza.aws.modules.amazonSNS.domain.dto.topics;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSnsTopicDto extends AuditInfoDto {
    @Size(max = 256)
    private String displayName;

    private Boolean fifoTopic;
    private Boolean contentBasedDeduplication;
    private String policy;
    private String attributesJson;
    private String tagsJson;
    private String status;
}
