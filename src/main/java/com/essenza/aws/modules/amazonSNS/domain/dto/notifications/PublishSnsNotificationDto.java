package com.essenza.aws.modules.amazonSNS.domain.dto.notifications;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
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
public class PublishSnsNotificationDto extends AuditInfoDto {
    @NotBlank
    private String topicArn;

    @Size(max = 256)
    private String subject;

    @NotBlank
    private String message;

    private Map<String, String> messageAttributes;
    private String messageGroupId;
    private String messageDeduplicationId;
}
