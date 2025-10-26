package com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon;

import com.essenza.aws.shared.common.domain.entity.AuditInfo;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;


@Data
@Entity
@Table(name = "bucket_aws")
public class BuketAwsEntity extends AuditInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    Long id;

    @Column(name = "project_name")
    String projectName;

    @Column(name = "application_id")
    Integer applicationId;

    @Column(name = "bucket_name")
    String bucketName;

    @Column(name = "bucket_folder")
    String bucketFolder;


}
