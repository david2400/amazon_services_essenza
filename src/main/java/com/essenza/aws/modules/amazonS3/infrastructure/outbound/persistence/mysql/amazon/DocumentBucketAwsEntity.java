package com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon;

import com.essenza.aws.shared.common.domain.entity.AuditInfo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "document_bucket_aws")
public class DocumentBucketAwsEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_document_bucket_aws", nullable = false)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Integer documentId;

    @Column(name = "bucket_aws_id", nullable = false)
    private Long bucketAwsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", referencedColumnName = "id_document", insertable = false, updatable = false)
    private DocumentEntity document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bucket_aws_id", referencedColumnName = "id", insertable = false, updatable = false)
    private BuketAwsEntity bucket;
}
