package com.essenza.aws.modules.amazonSNS.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.CreateSnsSubscriptionUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.DeleteSnsSubscriptionUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.FindSnsSubscriptionByIdUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.FindSnsSubscriptionsByTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.FindSnsSubscriptionsUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.subscriptions.UpdateSnsSubscriptionUseCase;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.CreateSnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.SnsSubscriptionDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.subscriptions.UpdateSnsSubscriptionDto;
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
@RequestMapping("/sns/subscriptions")
@Tag(name = "Amazon SNS Subscriptions", description = "Gestión de suscripciones a tópicos SNS")
public class SnsSubscriptionController {

    private final CreateSnsSubscriptionUseCase createUseCase;
    private final UpdateSnsSubscriptionUseCase updateUseCase;
    private final DeleteSnsSubscriptionUseCase deleteUseCase;
    private final FindSnsSubscriptionByIdUseCase findByIdUseCase;
    private final FindSnsSubscriptionsUseCase findAllUseCase;
    private final FindSnsSubscriptionsByTopicUseCase findByTopicUseCase;

    @PostMapping
    @Operation(summary = "Suscribir endpoint", description = "Crea una nueva suscripción a un tópico SNS")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Suscripción creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "502", description = "Error de AWS SNS")
    })
    public ResponseEntity<SnsSubscriptionDto> subscribe(@Valid @RequestBody CreateSnsSubscriptionDto dto) {
        SnsSubscriptionDto created = createUseCase.subscribe(dto);
        return ResponseEntity.created(URI.create("/sns/subscriptions/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar suscripción")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suscripción actualizada"),
            @ApiResponse(responseCode = "404", description = "Suscripción no encontrada")
    })
    public SnsSubscriptionDto update(@PathVariable Long id, @Valid @RequestBody UpdateSnsSubscriptionDto dto) {
        return updateUseCase.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar suscripción", description = "Cancela la suscripción de un endpoint")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Suscripción eliminada"),
            @ApiResponse(responseCode = "404", description = "Suscripción no encontrada")
    })
    public ResponseEntity<Void> unsubscribe(@PathVariable Long id) {
        boolean removed = deleteUseCase.unsubscribe(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "Listar suscripciones", description = "Lista todas las suscripciones paginadas o filtra por ARN de tópico")
    public Page<SnsSubscriptionDto> findAll(
            @Parameter(description = "Filtra por ARN del tópico")
            @RequestParam(value = "topicArn", required = false) String topicArn,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        if (topicArn != null) {
            return findByTopicUseCase.findByTopicArn(topicArn, pageable);
        }
        return findAllUseCase.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar suscripción por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suscripción encontrada"),
            @ApiResponse(responseCode = "404", description = "Suscripción no encontrada")
    })
    public ResponseEntity<SnsSubscriptionDto> findById(@PathVariable Long id) {
        return findByIdUseCase.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
