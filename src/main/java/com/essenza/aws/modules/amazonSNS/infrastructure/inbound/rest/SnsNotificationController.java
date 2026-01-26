package com.essenza.aws.modules.amazonSNS.infrastructure.inbound.rest;

import com.essenza.aws.modules.amazonSNS.application.input.notifications.FindSnsNotificationsByTopicUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.notifications.FindSnsNotificationsUseCase;
import com.essenza.aws.modules.amazonSNS.application.input.notifications.PublishSnsNotificationUseCase;
import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.PublishSnsNotificationDto;
import com.essenza.aws.modules.amazonSNS.domain.dto.notifications.SnsNotificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/sns/notifications")
@Tag(name = "Amazon SNS Notifications", description = "Publicación y consultas de mensajes SNS")
public class SnsNotificationController {

    private final PublishSnsNotificationUseCase publishUseCase;
    private final FindSnsNotificationsUseCase findAllUseCase;
    private final FindSnsNotificationsByTopicUseCase findByTopicUseCase;

//    public SnsNotificationController(PublishSnsNotificationUseCase publishUseCase,
//                                     FindSnsNotificationsUseCase findAllUseCase,
//                                     FindSnsNotificationsByTopicUseCase findByTopicUseCase) {
//        this.publishUseCase = publishUseCase;
//        this.findAllUseCase = findAllUseCase;
//        this.findByTopicUseCase = findByTopicUseCase;
//    }

    @PostMapping
    @Operation(summary = "Publicar notificación", description = "Publica un mensaje en un tópico SNS")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje publicado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<SnsNotificationDto> publish(@Valid @RequestBody PublishSnsNotificationDto dto) {
        SnsNotificationDto published = publishUseCase.publish(dto);
        return ResponseEntity.created(URI.create("/sns/notifications/" + published.getId())).body(published);
    }

    @GetMapping
    @Operation(summary = "Listar notificaciones", description = "Lista las notificaciones publicadas o filtra por tópico")
    public List<SnsNotificationDto> findAll(
            @Parameter(description = "Filtra por ARN del tópico")
            @RequestParam(value = "topicArn", required = false) String topicArn) {
        if (topicArn != null) {
            return findByTopicUseCase.findByTopicArn(topicArn);
        }
        return findAllUseCase.findAll();
    }
}
