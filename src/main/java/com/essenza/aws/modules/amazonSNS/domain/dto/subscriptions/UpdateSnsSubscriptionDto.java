package com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions;

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
public class UpdateSnsSubscriptionDto extends AuditInfoDto {
    @Size(max = 512)
    private String endpoint;

    private String filterPolicy;
    private Boolean rawMessageDelivery;
    private String deliveryStatus;
}
