package com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions;

import com.essenza.aws.shared.common.domain.dto.AuditInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SnsSubscriptionDto extends AuditInfoDto {
    private Long id;
    private String topicArn;
    private String protocol;
    private String endpoint;
    private String subscriptionArn;
    private String filterPolicy;
    private Boolean rawMessageDelivery;
    private String deliveryStatus;
}
