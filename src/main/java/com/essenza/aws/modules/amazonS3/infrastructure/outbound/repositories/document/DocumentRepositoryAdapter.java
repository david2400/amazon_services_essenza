package com.essenza.aws.modules.amazonS3.infrastructure.outbound.repositories.document;

import com.essenza.aws.modules.amazonS3.application.output.repository.DocumentRepository;
import com.essenza.aws.modules.amazonS3.domain.dto.document.CreateDocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.DocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.UpdateDocumentDto;
import com.essenza.aws.modules.amazonS3.infrastructure.outbound.mappers.DocumentMapper;
import com.essenza.aws.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentRepositoryAdapter implements DocumentRepository {

    private final JpaDocumentRepository jpaRepository;
    private final DocumentMapper mapper;

    @Override
    public DocumentDto create(CreateDocumentDto input) {
        var entity = mapper.toEntity(input);
        var saved = jpaRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public DocumentDto update(Integer id, UpdateDocumentDto input) {
        var entity = jpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Documento no encontrado con id " + id));
        mapper.updateEntityFromDto(input, entity);
        var updated = jpaRepository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    public boolean deleteById(Integer id) {
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<DocumentDto> findById(Integer id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public Page<DocumentDto> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Optional<DocumentDto> findByName(String name) {
        return jpaRepository.findByName(name).map(mapper::toDto);
    }
}
