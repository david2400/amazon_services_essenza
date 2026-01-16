package com.essenza.aws.modules.amazonS3.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonS3.application.input.document_bucket_aws.*;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.CreateDocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.DocumentBucketAwsDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document_bucket_aws.UpdateDocumentBucketAwsDto;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/document_bucket_aws")
@Description("Relación entre documentos y buckets de AWS")
public class DocumentBucketAwsController {

    private final CreateDocumentBucketAwsUseCase createUseCase;
    private final UpdateDocumentBucketAwsUseCase updateUseCase;
    private final DeleteDocumentBucketAwsUseCase deleteUseCase;
    private final FindDocumentBucketAwsByIdUseCase findByIdUseCase;
    private final FindDocumentBucketAwsUseCase findAllUseCase;
    private final FindDocumentBucketAwsByDocumentIdUseCase findByDocumentIdUseCase;
    private final FindDocumentBucketAwsByBucketAwsIdUseCase findByBucketAwsIdUseCase;

    public DocumentBucketAwsController(CreateDocumentBucketAwsUseCase createUseCase,
                                       UpdateDocumentBucketAwsUseCase updateUseCase,
                                       DeleteDocumentBucketAwsUseCase deleteUseCase,
                                       FindDocumentBucketAwsByIdUseCase findByIdUseCase,
                                       FindDocumentBucketAwsUseCase findAllUseCase,
                                       FindDocumentBucketAwsByDocumentIdUseCase findByDocumentIdUseCase,
                                       FindDocumentBucketAwsByBucketAwsIdUseCase findByBucketAwsIdUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.findAllUseCase = findAllUseCase;
        this.findByDocumentIdUseCase = findByDocumentIdUseCase;
        this.findByBucketAwsIdUseCase = findByBucketAwsIdUseCase;
    }

    @PostMapping
    public ResponseEntity<DocumentBucketAwsDto> create(@RequestBody CreateDocumentBucketAwsDto dto) {
        DocumentBucketAwsDto created = createUseCase.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentBucketAwsDto> update(@PathVariable("id") Long id,
                                                       @RequestBody UpdateDocumentBucketAwsDto dto) {
        DocumentBucketAwsDto updated = updateUseCase.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        boolean deleted = deleteUseCase.deleteById(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentBucketAwsDto> findById(@PathVariable("id") Long id) {
        Optional<DocumentBucketAwsDto> result = findByIdUseCase.findById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<List<DocumentBucketAwsDto>> findAll(
            @RequestParam(value = "document_id", required = false) Integer documentId,
            @RequestParam(value = "bucket_aws_id", required = false) Long bucketAwsId) {
        if (documentId != null) {
            return ResponseEntity.ok(findByDocumentIdUseCase.findByDocumentId(documentId));
        }
        if (bucketAwsId != null) {
            return ResponseEntity.ok(findByBucketAwsIdUseCase.findByBucketAwsId(bucketAwsId));
        }
        List<DocumentBucketAwsDto> items = findAllUseCase.findAll();
        if (items.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(items);
    }
}
