package com.essenza.aws.modules.amazonSNS.domain.dto.notifications;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SnsNotificationDto extends AuditInfoDto {
    private Long id;
    private String topicArn;
    private String subject;
    private String message;
    private Map<String, String> messageAttributes;
    private String messageGroupId;
    private String messageDeduplicationId;
    private String messageId;
    private String status;
    private String errorMessage;
}
