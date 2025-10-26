package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.document;

import com.essenza.aws.modules.amazonS3.infrastructure.outbound.persistence.mysql.amazon.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaDocumentRepository extends JpaRepository<DocumentEntity, Integer> {
    Optional<DocumentEntity> findByName(String name);
}
