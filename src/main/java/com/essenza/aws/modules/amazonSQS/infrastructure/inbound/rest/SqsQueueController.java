package com.essenza.aws.modules.amazonSQS.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonSQS.application.input.queues.CreateSqsQueueUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.queues.DeleteSqsQueueUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.queues.FindSqsQueueByIdUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.queues.FindSqsQueueByUrlUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.queues.FindSqsQueuesUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.queues.UpdateSqsQueueUseCase;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.CreateSqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.SqsQueueDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.queues.UpdateSqsQueueDto;
import com.essenza.aws.shared.exceptions.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sqs/queues")
@Tag(name = "Amazon SQS Queues", description = "CRUD y consultas de colas de Amazon SQS")
public class SqsQueueController {

    private final CreateSqsQueueUseCase createUseCase;
    private final UpdateSqsQueueUseCase updateUseCase;
    private final DeleteSqsQueueUseCase deleteUseCase;
    private final FindSqsQueuesUseCase findAllUseCase;
    private final FindSqsQueueByIdUseCase findByIdUseCase;
    private final FindSqsQueueByUrlUseCase findByUrlUseCase;

    @PostMapping
    @Operation(summary = "Crear cola", description = "Crea una cola estándar o FIFO en Amazon SQS con atributos y tags")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cola creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre, parámetros FIFO, RedrivePolicy)"),
            @ApiResponse(responseCode = "502", description = "Error de AWS SQS")
    })
    public ResponseEntity<SqsQueueDto> create(@Valid @RequestBody CreateSqsQueueDto dto) {
        SqsQueueDto created = createUseCase.create(dto);
        return ResponseEntity.created(URI.create("/sqs/queues/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cola",
            description = "Actualiza atributos de una cola existente. FifoQueue y ContentBasedDeduplication no son actualizables")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cola actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o atributo no actualizable"),
            @ApiResponse(responseCode = "404", description = "Cola no encontrada"),
            @ApiResponse(responseCode = "502", description = "Error de AWS SQS")
    })
    public SqsQueueDto update(@PathVariable Long id, @Valid @RequestBody UpdateSqsQueueDto dto) {
        return updateUseCase.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cola", description = "Elimina la cola en AWS y la marca como eliminada en BD")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cola eliminada"),
            @ApiResponse(responseCode = "404", description = "Cola no encontrada"),
            @ApiResponse(responseCode = "502", description = "Error de AWS SQS")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = deleteUseCase.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "Listar colas", description = "Obtiene todas las colas paginadas o filtra por URL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de colas"),
            @ApiResponse(responseCode = "404", description = "Cola no encontrada (filtro por URL)")
    })
    public Page<SqsQueueDto> findAll(
            @Parameter(description = "Filtra por URL exacta de la cola")
            @RequestParam(value = "queueUrl", required = false) String queueUrl,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        if (queueUrl != null) {
            SqsQueueDto queue = findByUrlUseCase.findByQueueUrl(queueUrl)
                    .orElseThrow(() -> new NotFoundException("SQS Queue not found with url " + queueUrl));
            return new PageImpl<>(List.of(queue), pageable, 1);
        }
        return findAllUseCase.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cola por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cola encontrada"),
            @ApiResponse(responseCode = "404", description = "Cola no encontrada")
    })
    public ResponseEntity<SqsQueueDto> findById(@PathVariable Long id) {
        return findByIdUseCase.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
