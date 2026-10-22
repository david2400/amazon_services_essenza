package com.essenza.aws.modules.amazonSNS.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonSNS.application.input.topics.CreateSnsTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.DeleteSnsTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.FindSnsTopicByArnUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.FindSnsTopicByIdUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.FindSnsTopicsUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.topics.UpdateSnsTopicUseCase;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.CreateSnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.SnsTopicDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.topics.UpdateSnsTopicDto;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/sns/topics")
@Tag(name = "Amazon SNS Topics", description = "CRUD y consultas de tópicos de Amazon SNS")
public class SnsTopicController {

    private final CreateSnsTopicUseCase createUseCase;
    private final UpdateSnsTopicUseCase updateUseCase;
    private final DeleteSnsTopicUseCase deleteUseCase;
    private final FindSnsTopicsUseCase findAllUseCase;
    private final FindSnsTopicByIdUseCase findByIdUseCase;
    private final FindSnsTopicByArnUseCase findByArnUseCase;

    @PostMapping
    @Operation(summary = "Crear tópico", description = "Crea un nuevo tópico en Amazon SNS")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tópico creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "502", description = "Error de AWS SNS")
    })
    public ResponseEntity<SnsTopicDto> create(@Valid @RequestBody CreateSnsTopicDto dto) {
        SnsTopicDto created = createUseCase.create(dto);
        return ResponseEntity.created(URI.create("/sns/topics/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tópico", description = "Actualiza metadatos de un tópico existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tópico actualizado"),
            @ApiResponse(responseCode = "404", description = "Tópico no encontrado")
    })
    public SnsTopicDto update(@PathVariable Long id, @Valid @RequestBody UpdateSnsTopicDto dto) {
        return updateUseCase.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tópico", description = "Elimina un tópico por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tópico eliminado"),
            @ApiResponse(responseCode = "404", description = "Tópico no encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = deleteUseCase.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "Listar tópicos", description = "Obtiene todos los tópicos paginados o filtra por ARN")
    public Page<SnsTopicDto> findAll(
            @Parameter(description = "Filtra por ARN exacto")
            @RequestParam(value = "arn", required = false) String arn,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        if (arn != null) {
            SnsTopicDto topic = findByArnUseCase.findByArn(arn)
                    .orElseThrow(() -> new NotFoundException("SNS Topic not found with arn " + arn));
            return new org.springframework.data.domain.PageImpl<>(java.util.List.of(topic), pageable, 1);
        }
        return findAllUseCase.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tópico por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tópico encontrado"),
            @ApiResponse(responseCode = "404", description = "Tópico no encontrado")
    })
    public ResponseEntity<SnsTopicDto> findById(@PathVariable Long id) {
        return findByIdUseCase.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
