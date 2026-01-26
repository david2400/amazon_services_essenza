package com.essenza.aws.modules.amazonSNS.domain.dto.topics;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SnsTopicDto extends AuditInfoDto {
    private Long id;
    private String name;
    private String arn;
    private String displayName;
    private String policy;
    private Boolean fifoTopic;
    private Boolean contentBasedDeduplication;
    private String attributesJson;
    private String tagsJson;
    private String status;
}
