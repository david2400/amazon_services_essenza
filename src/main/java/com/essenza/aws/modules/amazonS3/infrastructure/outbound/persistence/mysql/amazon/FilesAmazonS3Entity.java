package com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon;

import com.essenza.aws.shared.common.domain.entity.AuditInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "amazon_s3_files")
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

    //@NotNull
    @Column(name = "`key`")
    String key;

    //@NotNull
    @Column(name = "document_id")
    Integer documentId;

    @Column(name = "relation_type_id")
    Integer relationTypeId;

    @Column(name = "expiration_date")
    Date expirationDate;

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

    //@NotNull
    @Column(name = "bucket_aws_id")
    Long bucketAwsId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bucket_aws_id", insertable = false, updatable = false)
    private BuketAwsEntity bucketAwsEntity;

    @Column(name = "days_since_creation", updatable = false, insertable = false)
    Integer daysSinceCreation;

    @Column(name = "days_until_expiration", updatable = false, insertable = false)
    Integer daysUntilExpiration;

}
