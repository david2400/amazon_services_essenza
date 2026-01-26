package com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSnsSubscriptionDto extends AuditInfoDto {
    @NotBlank
    private String topicArn;

    @NotBlank
    @Pattern(regexp = "http|https|email|email-json|sms|sqs|application|lambda|firehose")
    private String protocol;

    @NotBlank
    @Size(max = 512)
    private String endpoint;

    private String filterPolicy;
    private Boolean rawMessageDelivery;
    @Size(max = 512)
    private String subscriptionArn;
    @Size(max = 50)
    private String deliveryStatus;
}
