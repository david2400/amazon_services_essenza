package com.essenza.aws.modules.amazonSQS.domain.dto.queues;

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
public class SqsQueueDto extends AuditInfoDto {
    private Long id;
    private String name;
    private String queueUrl;
    private String arn;
    private Integer delaySeconds;
    private Integer maximumMessageSize;
    private Integer messageRetentionPeriod;
    private Integer visibilityTimeout;
    private Boolean fifoQueue;
    private Boolean contentBasedDeduplication;
    private String redrivePolicy;
    private Map<String, String> attributes;
    private Map<String, String> tags;
    private String status;
}
