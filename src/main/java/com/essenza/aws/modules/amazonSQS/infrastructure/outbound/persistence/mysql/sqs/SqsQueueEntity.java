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
@Table(name = "sqs_queues", indexes = {
        @Index(name = "idx_sqs_queue_name", columnList = "name"),
        @Index(name = "idx_sqs_queue_status", columnList = "status")
})
public class SqsQueueEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sqs_queue")
    private Long id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "queue_url", nullable = false, unique = true, length = 512)
    private String queueUrl;

    @Column(name = "arn", length = 512)
    private String arn;

    @Column(name = "delay_seconds")
    private Integer delaySeconds;

    @Column(name = "maximum_message_size")
    private Integer maximumMessageSize;

    @Column(name = "message_retention_period")
    private Integer messageRetentionPeriod;

    @Column(name = "visibility_timeout")
    private Integer visibilityTimeout;

    @Column(name = "fifo_queue")
    private Boolean fifoQueue;

    @Column(name = "content_based_deduplication")
    private Boolean contentBasedDeduplication;

    @Column(name = "redrive_policy", columnDefinition = "TEXT")
    private String redrivePolicy;

    @Column(name = "attributes_json", columnDefinition = "TEXT")
    private String attributesJson;

    @Column(name = "tags_json", columnDefinition = "TEXT")
    private String tagsJson;

    @Column(name = "status", length = 50)
    private String status;
}
