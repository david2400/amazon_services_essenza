package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.relation_type;

import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.RelationTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaRelationTypeRepository extends JpaRepository<RelationTypeEntity, Integer> {
    Optional<RelationTypeEntity> findByName(String name);
}
