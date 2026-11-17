package com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon;

import com.essenza.aws.shared.common.domain.entity.AuditInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Objeto físico almacenado en S3. Una misma pieza de contenido (mismo SHA-256 dentro de un
 * bucket registrado) se almacena una sola vez; cada fila de {@code amazon_s3_files} es una
 * referencia lógica que apunta aquí vía {@code s3_object_id}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "s3_objects",
        uniqueConstraints = @UniqueConstraint(name = "uk_s3_objects_bucket_key", columnNames = {"bucket_aws_id", "object_key"}),
        indexes = {
                @Index(name = "idx_s3_objects_hash", columnList = "bucket_aws_id, content_hash"),
                @Index(name = "idx_s3_objects_object_key", columnList = "object_key")
        })
@Entity
public class S3ObjectEntity extends AuditInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_s3_object", unique = true)
    Long id;

    @Column(name = "bucket_aws_id", nullable = false)
    Long bucketAwsId;

    @Column(name = "object_key", nullable = false, length = 1024)
    String objectKey;

    /** SHA-256 en hexadecimal del contenido del archivo. Base de la deduplicación. */
    @Column(name = "content_hash", nullable = false, length = 64)
    String contentHash;

    @Column(name = "size_bytes")
    Long sizeBytes;

    @Column(name = "content_type")
    String contentType;

    @Column(name = "e_tag")
    String eTag;

    @Column(name = "s3_url", length = 2048)
    String s3Url;
}
