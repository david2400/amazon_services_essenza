package com.essenza.aws.modules.amazonS3.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonS3.application.input.document_bucket_aws.*;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.CreateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.UpdateDocumentBucketAwsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/document_bucket_aws")
@Description("Relación entre documentos y buckets de AWS")
@Tag(name = "Amazon S3 - Documentos por bucket")
public class DocumentBucketAwsController {

    private final CreateDocumentBucketAwsUseCase createUseCase;
    private final UpdateDocumentBucketAwsUseCase updateUseCase;
    private final DeleteDocumentBucketAwsUseCase deleteUseCase;
    private final FindDocumentBucketAwsByIdUseCase findByIdUseCase;
    private final FindDocumentBucketAwsUseCase findAllUseCase;
    private final FindDocumentBucketAwsByDocumentIdUseCase findByDocumentIdUseCase;
    private final FindDocumentBucketAwsByBucketAwsIdUseCase findByBucketAwsIdUseCase;

    @PostMapping
    @Operation(summary = "Crear relación documento-bucket")
    public ResponseEntity<DocumentBucketAwsDto> create(@Valid @RequestBody CreateDocumentBucketAwsDto dto) {
        DocumentBucketAwsDto created = createUseCase.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{documentId}/{bucketAwsId}")
    @Operation(summary = "Actualizar relación documento-bucket")
    public ResponseEntity<DocumentBucketAwsDto> update(@PathVariable("documentId") Integer documentId,
                                                       @PathVariable("bucketAwsId") Long bucketAwsId,
                                                       @Valid @RequestBody UpdateDocumentBucketAwsDto dto) {
        DocumentBucketAwsDto updated = updateUseCase.update(documentId, bucketAwsId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{documentId}/{bucketAwsId}")
    @Operation(summary = "Eliminar relación documento-bucket")
    public ResponseEntity<Void> delete(@PathVariable("documentId") Integer documentId,
                                       @PathVariable("bucketAwsId") Long bucketAwsId) {
        boolean deleted = deleteUseCase.deleteById(documentId, bucketAwsId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/{documentId}/{bucketAwsId}")
    @Operation(summary = "Consultar relación por su clave compuesta")
    public ResponseEntity<DocumentBucketAwsDto> findById(@PathVariable("documentId") Integer documentId,
                                                       @PathVariable("bucketAwsId") Long bucketAwsId) {
        Optional<DocumentBucketAwsDto> result = findByIdUseCase.findById(documentId, bucketAwsId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    @Operation(summary = "Listar relaciones filtrando por documento o bucket (paginado)")
    public Page<DocumentBucketAwsDto> findAll(
            @RequestParam(value = "document_id", required = false) Integer documentId,
            @RequestParam(value = "bucket_aws_id", required = false) Long bucketAwsId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        if (documentId != null) {
            return findByDocumentIdUseCase.findByDocumentId(documentId, pageable);
        }
        if (bucketAwsId != null) {
            return findByBucketAwsIdUseCase.findByBucketAwsId(bucketAwsId, pageable);
        }
        return findAllUseCase.findAll(pageable);
    }
}
