package com.essenza.aws.modules.amazonSQS.infrastructure.outbound.persistence.mysql.sqs;

import com.essenza.aws.shared.common.domain.entity.AuditInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sqs_messages", indexes = {
        @Index(name = "idx_sqs_msg_queue_url", columnList = "queue_url"),
        @Index(name = "idx_sqs_msg_status", columnList = "status"),
        @Index(name = "idx_sqs_msg_message_id", columnList = "message_id")
})
public class SqsMessageEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sqs_message")
    private Long id;

    @Column(name = "operation", nullable = false, length = 20)
    private String operation;

    @Column(name = "queue_url", nullable = false, length = 512)
    private String queueUrl;

    // MEDIUMTEXT: SQS admite cuerpos de hasta 1 MiB, TEXT solo llega a 64 KB.
    @Column(name = "message_body", columnDefinition = "MEDIUMTEXT")
    private String messageBody;

    @Column(name = "message_attributes_json", columnDefinition = "TEXT")
    private String messageAttributesJson;

    @Column(name = "message_group_id", length = 128)
    private String messageGroupId;

    @Column(name = "message_deduplication_id", length = 128)
    private String messageDeduplicationId;

    @Column(name = "delay_seconds")
    private Integer delaySeconds;

    @Column(name = "message_id", length = 100)
    private String messageId;

    @Column(name = "receipt_handle", columnDefinition = "TEXT")
    private String receiptHandle;

    @Column(name = "md5_of_body", length = 64)
    private String md5OfBody;

    @Column(name = "sequence_number", length = 128)
    private String sequenceNumber;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
