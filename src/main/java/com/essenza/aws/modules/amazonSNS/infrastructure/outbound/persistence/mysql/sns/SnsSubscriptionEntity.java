package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.persistence.mysql.sns;

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
@Table(name = "sns_subscriptions", indexes = {
        @Index(name = "idx_sns_sub_topic_arn", columnList = "topic_arn")
})
public class SnsSubscriptionEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sns_subscription")
    private Long id;

    @Column(name = "topic_arn", nullable = false, length = 512)
    private String topicArn;

    @Column(name = "protocol", nullable = false, length = 50)
    private String protocol;

    @Column(name = "endpoint", nullable = false, length = 512)
    private String endpoint;

    @Column(name = "subscription_arn", length = 512, unique = true)
    private String subscriptionArn;

    @Column(name = "filter_policy", columnDefinition = "TEXT")
    private String filterPolicy;

    @Column(name = "raw_message_delivery")
    private Boolean rawMessageDelivery;

    @Column(name = "delivery_status", length = 50)
    private String deliveryStatus;
}
