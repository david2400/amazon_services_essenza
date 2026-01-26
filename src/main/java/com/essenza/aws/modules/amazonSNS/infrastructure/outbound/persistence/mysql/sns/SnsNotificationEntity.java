package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.persistence.mysql.sns;

import com.essenza.aws.shared.common.domain.entity.AuditInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sns_notifications")
public class SnsNotificationEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sns_notification")
    private Long id;

    @Column(name = "topic_arn", nullable = false, length = 512)
    private String topicArn;

    @Column(name = "subject", length = 256)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "message_attributes_json", columnDefinition = "TEXT")
    private String messageAttributesJson;

    @Column(name = "message_group_id", length = 128)
    private String messageGroupId;

    @Column(name = "message_deduplication_id", length = 128)
    private String messageDeduplicationId;

    @Column(name = "status", length = 50)
    private String status;
}
