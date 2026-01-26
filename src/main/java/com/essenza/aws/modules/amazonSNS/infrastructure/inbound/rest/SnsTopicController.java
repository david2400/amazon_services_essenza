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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/sns/topics")
@Tag(name = "Amazon SNS Topics", description = "CRUD y consultas de tópicos de Amazon SNS")
public class SnsTopicController {

    private final CreateSnsTopicUseCase createUseCase;
    private final UpdateSnsTopicUseCase updateUseCase;
    private final DeleteSnsTopicUseCase deleteUseCase;
    private final FindSnsTopicsUseCase findAllUseCase;
    private final FindSnsTopicByIdUseCase findByIdUseCase;
    private final FindSnsTopicByArnUseCase findByArnUseCase;

//    public SnsTopicController(CreateSnsTopicUseCase createUseCase,
//                              UpdateSnsTopicUseCase updateUseCase,
//                              DeleteSnsTopicUseCase deleteUseCase,
//                              FindSnsTopicsUseCase findAllUseCase,
//                              FindSnsTopicByIdUseCase findByIdUseCase,
//                              FindSnsTopicByArnUseCase findByArnUseCase) {
//        this.createUseCase = createUseCase;
//        this.updateUseCase = updateUseCase;
//        this.deleteUseCase = deleteUseCase;
//        this.findAllUseCase = findAllUseCase;
//        this.findByIdUseCase = findByIdUseCase;
//        this.findByArnUseCase = findByArnUseCase;
//    }

    @PostMapping
    @Operation(summary = "Crear tópico", description = "Crea un nuevo tópico en Amazon SNS")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tópico creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
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
    @Operation(summary = "Listar tópicos", description = "Obtiene todos los tópicos o filtra por ARN")
    public List<SnsTopicDto> findAll(
            @Parameter(description = "Filtra por ARN exacto")
            @RequestParam(value = "arn", required = false) String arn) {
        if (arn != null) {
            return findByArnUseCase.findByArn(arn).map(List::of).orElseGet(List::of);
        }
        return findAllUseCase.findAll();
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
