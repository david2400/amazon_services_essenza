package com.essenza.aws.modules.amazonSQS.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonSQS.application.input.messages.DeleteSqsMessageUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.messages.FindSqsMessagesByQueueUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.messages.FindSqsMessagesUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.messages.ReceiveSqsMessagesUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.messages.SendSqsMessageBatchUseCase;
import com.essenza.aws.modules.amazonSQS.application.input.messages.SendSqsMessageUseCase;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.DeleteSqsMessageDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.ReceiveSqsMessagesDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SendSqsMessageDto;
import com.essenza.aws.modules.amazonSQS.domain.dto.messages.SqsMessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sqs/messages")
@Tag(name = "Amazon SQS Messages", description = "Envío, recepción, borrado y consultas de mensajes SQS")
public class SqsMessageController {

    private final SendSqsMessageUseCase sendUseCase;
    private final SendSqsMessageBatchUseCase sendBatchUseCase;
    private final ReceiveSqsMessagesUseCase receiveUseCase;
    private final DeleteSqsMessageUseCase deleteUseCase;
    private final FindSqsMessagesUseCase findAllUseCase;
    private final FindSqsMessagesByQueueUseCase findByQueueUseCase;

    @PostMapping
    @Operation(summary = "Enviar mensaje", description = "Envía un mensaje a una cola SQS")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje enviado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (parámetros FIFO, tamaño)"),
            @ApiResponse(responseCode = "502", description = "Error de AWS SQS")
    })
    public ResponseEntity<SqsMessageDto> send(@Valid @RequestBody SendSqsMessageDto dto) {
        SqsMessageDto sent = sendUseCase.send(dto);
        return ResponseEntity.created(URI.create("/sqs/messages/" + sent.getId())).body(sent);
    }

    @PostMapping("/batch")
    @Operation(summary = "Enviar mensajes en batch",
            description = "Envía hasta 100 mensajes; internamente se agrupan por cola y se envían en lotes de 10 (límite de SQS)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch procesado (revisar status de cada mensaje)"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "502", description = "Error de AWS SQS")
    })
    public List<SqsMessageDto> sendBatch(
            @Valid @RequestBody @Size(min = 1, max = 100) List<SendSqsMessageDto> dtos) {
        return sendBatchUseCase.sendBatch(dtos);
    }

    @PostMapping("/receive")
    @Operation(summary = "Recibir mensajes",
            description = "Recibe hasta 10 mensajes con long polling opcional (waitTimeSeconds ≤ 20) y visibility timeout configurable")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensajes recibidos (lista vacía si no hay mensajes)"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "502", description = "Error de AWS SQS")
    })
    public List<SqsMessageDto> receive(@Valid @RequestBody ReceiveSqsMessagesDto dto) {
        return receiveUseCase.receive(dto);
    }

    @PostMapping("/delete")
    @Operation(summary = "Eliminar mensaje", description = "Elimina un mensaje de la cola usando su receiptHandle")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensaje eliminado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "502", description = "Error de AWS SQS (p. ej. receiptHandle inválido o expirado)")
    })
    public SqsMessageDto delete(@Valid @RequestBody DeleteSqsMessageDto dto) {
        return deleteUseCase.delete(dto);
    }

    @GetMapping
    @Operation(summary = "Listar mensajes", description = "Lista el registro de mensajes enviados/recibidos o filtra por cola")
    public Page<SqsMessageDto> findAll(
            @Parameter(description = "Filtra por URL de la cola")
            @RequestParam(value = "queueUrl", required = false) String queueUrl,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        if (queueUrl != null) {
            return findByQueueUseCase.findByQueueUrl(queueUrl, pageable);
        }
        return findAllUseCase.findAll(pageable);
    }
}
