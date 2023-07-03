package ru.altagroup.notificationcenter.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.altagroup.notificationcenter.dto.ServiceNotificationDto;
import ru.altagroup.notificationcenter.dto.UpdateServiceNotificationDto;
import ru.altagroup.notificationcenter.services.NotificationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Контроллер уведомлений")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/recipients/notices", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получить настройки уведомлений")
    public ResponseEntity<List<ServiceNotificationDto>> getNotificationSettings(JwtAuthenticationToken principal) {
        UUID recipientId = UUID.fromString(principal.getName());
        List<ServiceNotificationDto> notifications = notificationService.getNotificationsSettings(recipientId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping(value = "/recipients/notice", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Обновить настройки уведомлений")
    public ResponseEntity<ServiceNotificationDto> updateNotificationSettings(@RequestBody @Valid UpdateServiceNotificationDto update,
                                                                             JwtAuthenticationToken principal) {
        UUID recipientId = UUID.fromString(principal.getName());
        ServiceNotificationDto notification = notificationService.updateNotificationSettings(recipientId, update);
        return ResponseEntity.ok(notification);
    }
}
