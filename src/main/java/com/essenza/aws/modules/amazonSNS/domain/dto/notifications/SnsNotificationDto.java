package com.essenza.aws.modules.amazonSNS.domain.dto.notifications;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SnsNotificationDto extends AuditInfoDto {
    private Long id;
    private String topicArn;
    private String subject;
    private String message;
    private String messageAttributesJson;
    private String messageGroupId;
    private String messageDeduplicationId;
    private String status;
}
