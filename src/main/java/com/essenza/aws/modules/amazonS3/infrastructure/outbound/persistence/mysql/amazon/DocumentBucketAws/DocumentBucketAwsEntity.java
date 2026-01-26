package com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.DocumentBucketAws;

import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.BuketAwsEntity;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.DocumentEntity;
import com.essenza.aws.shared.common.domain.entity.AuditInfo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "document_bucket_aws")
@IdClass(DocumentBucketAwsId.class)
public class DocumentBucketAwsEntity extends AuditInfo {

    @Id
    @Column(name = "document_id", nullable = false)
    private Integer documentId;

    @Id
    @Column(name = "bucket_aws_id", nullable = false)
    private Long bucketAwsId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", referencedColumnName = "id_document", insertable = false, updatable = false)
    private DocumentEntity document;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bucket_aws_id", referencedColumnName = "id", insertable = false, updatable = false)
    private BuketAwsEntity bucket;
}
