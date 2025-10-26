package com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon;

import com.essenza.aws.shared.common.domain.entity.AuditInfo;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "documents")
public class DocumentEntity extends AuditInfo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_document", nullable = false)
  private Integer id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Lob
  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "new_document", nullable = false)
  private String newDocument;

  @Column(name = "validity_days")
  private Integer validityDays;

  @Column(name = "is_document_validity")
  private Character isDocumentValidity;

}