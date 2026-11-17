package com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon;

import com.essenza.aws.shared.common.domain.entity.AuditInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "amazon_s3_files",
        indexes = {
                @Index(name = "idx_s3_files_key_bucket", columnList = "`key`, bucket_aws_id"),
                @Index(name = "idx_s3_files_document_id", columnList = "document_id"),
                @Index(name = "idx_s3_files_bucket_aws_id", columnList = "bucket_aws_id"),
                @Index(name = "idx_s3_files_parent_id", columnList = "parent_id"),
                @Index(name = "idx_s3_files_s3_object_id", columnList = "s3_object_id")
        })
@Entity
public class FilesAmazonS3Entity extends AuditInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_files_amazon_s3", unique = true)
    Long id;

    @Column(name = "parent_id")
    Long parentId;

    @Column(name = "file_name")
    String fileName;

    @Column(name = "`key`")
    String key;

    @Column(name = "document_id")
    Integer documentId;

    @Column(name = "expiration_date")
    Instant expirationDate;

    @Column(name = "e_tag")
    String eTag;

    @Column(name = "route_aws")
    String routeAws;

    @Column(name = "status", insertable = false, updatable = false)
    String status;

    @Column(name = "s3_url")
    String s3Url;

    @Column(name = "reference")
    Integer reference;

    @Column(name = "bucket_aws_id", nullable = false)
    Long bucketAwsId;

    /** FK lógica al objeto físico en {@code s3_objects} (deduplicación por contenido). */
    @Column(name = "s3_object_id")
    Long s3ObjectId;

    /** SHA-256 del contenido, denormalizado desde {@code s3_objects} para lecturas sin join. */
    @Column(name = "content_hash", length = 64)
    String contentHash;

    @Column(name = "size_bytes")
    Long sizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bucket_aws_id", insertable = false, updatable = false)
    private BucketAwsEntity bucketAwsEntity;

    @Column(name = "days_since_creation", updatable = false, insertable = false)
    Integer daysSinceCreation;

    @Column(name = "days_until_expiration", updatable = false, insertable = false)
    Integer daysUntilExpiration;

}
