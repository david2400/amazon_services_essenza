package com.essenza.aws.modules.amazonSQS.domain.dto.messages;

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
public class SqsMessageDto extends AuditInfoDto {
    private Long id;
    /** SEND, RECEIVE o DELETE. */
    private String operation;
    private String queueUrl;
    private String messageBody;
    private Map<String, String> messageAttributes;
    private String messageGroupId;
    private String messageDeduplicationId;
    private Integer delaySeconds;
    private String messageId;
    private String receiptHandle;
    private String md5OfBody;
    private String sequenceNumber;
    /** SENT, RECEIVED, DELETED o FAILED. */
    private String status;
    private String errorMessage;
}
