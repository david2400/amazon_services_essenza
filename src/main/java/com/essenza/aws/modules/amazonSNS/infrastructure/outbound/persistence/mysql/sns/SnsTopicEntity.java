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
@Table(name = "sns_topics")
public class SnsTopicEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sns_topic")
    private Long id;

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "arn", nullable = false, unique = true, length = 512)
    private String arn;

    @Column(name = "display_name", length = 256)
    private String displayName;

    @Column(name = "policy", columnDefinition = "TEXT")
    private String policy;

    @Column(name = "fifo_topic")
    private Boolean fifoTopic;

    @Column(name = "content_based_deduplication")
    private Boolean contentBasedDeduplication;

    @Column(name = "attributes_json", columnDefinition = "TEXT")
    private String attributesJson;

    @Column(name = "tags_json", columnDefinition = "TEXT")
    private String tagsJson;

    @Column(name = "status", length = 50)
    private String status;
}
