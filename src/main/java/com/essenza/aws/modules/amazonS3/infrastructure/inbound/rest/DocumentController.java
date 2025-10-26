package com.essenza.aws.modules.amazonS3.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonS3.application.input.document.CreateDocumentUseCase;
import com.essenza.aws.modules.amazonS3.application.input.document.DeleteDocumentUseCase;
import com.essenza.aws.modules.amazonS3.application.input.document.FindDocumentByIdUseCase;
import com.essenza.aws.modules.amazonS3.application.input.document.FindDocumentsUseCase;
import com.essenza.aws.modules.amazonS3.application.input.document.UpdateDocumentUseCase;
import com.essenza.aws.modules.amazonS3.domain.dto.document.CreateDocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.DocumentDto;
import com.essenza.aws.modules.amazonS3.domain.dto.document.UpdateDocumentDto;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/document")
@Description(value = "Tipo documentos adjuntos")
public class DocumentController {

    private final CreateDocumentUseCase createDocument;
    private final UpdateDocumentUseCase updateDocument;
    private final DeleteDocumentUseCase deleteDocument;
    private final FindDocumentByIdUseCase findById;
    private final FindDocumentsUseCase findAll;

    public DocumentController(CreateDocumentUseCase createDocument,
                              UpdateDocumentUseCase updateDocument,
                              DeleteDocumentUseCase deleteDocument,
                              FindDocumentByIdUseCase findById,
                              FindDocumentsUseCase findAll) {
        this.createDocument = createDocument;
        this.updateDocument = updateDocument;
        this.deleteDocument = deleteDocument;
        this.findById = findById;
        this.findAll = findAll;
    }

    @PostMapping
    public ResponseEntity<DocumentDto> createDocumento(@RequestBody CreateDocumentDto documentoDto) {
        DocumentDto nuevoDocumento = createDocument.create(documentoDto);
        return new ResponseEntity<>(nuevoDocumento, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentDto> updateDocumento(@PathVariable Integer id, @RequestBody UpdateDocumentDto documentoDto) {
        DocumentDto documentoActualizado = updateDocument.update(id, documentoDto);
        return new ResponseEntity<>(documentoActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteDocumento(@PathVariable Integer id) {
        boolean deleted = deleteDocument.deleteById(id);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocumentoById(@PathVariable Integer id) {
        Optional<DocumentDto> documento = findById.findById(id);
        return documento.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto>> getAllDocumentos() {
        List<DocumentDto> documentos = findAll.findAll();
        if (documentos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(documentos, HttpStatus.OK);
    }


}