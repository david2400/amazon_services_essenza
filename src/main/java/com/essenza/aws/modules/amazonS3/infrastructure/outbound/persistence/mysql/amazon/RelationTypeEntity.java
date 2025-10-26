package com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon;

import com.essenza.aws.shared.common.domain.entity.AuditInfo;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "relation_types")
public class RelationTypeEntity extends AuditInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_relation_type", nullable = false)
    private Integer id;

    @Column(name = "name", length = 50)
    private String name;

    // @Column(name = "relation_type_id")
//     Integer relationTypeId;

}