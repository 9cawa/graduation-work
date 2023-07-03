package ru.altagroup.notificationcenter.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.altagroup.notificationcenter.dto.DndRequest;
import ru.altagroup.notificationcenter.dto.DndResponse;
import ru.altagroup.notificationcenter.services.DndService;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Контроллер статуса \"не беспокоить\"")
public class DndController {

    private final DndService dndService;

    @GetMapping(value = "/recipients/dnd", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получить информацию о статусе \"не беспокоить\"")
    public ResponseEntity<DndResponse> getDndStatus(JwtAuthenticationToken principal) {
        UUID recipientId = UUID.fromString(principal.getName());
        DndResponse response = dndService.getByRecipient(recipientId);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/recipients/dnd", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Установить статус \"не беспокоить\"")
    public ResponseEntity<DndResponse> setDndStatus(@RequestBody @Valid DndRequest dndRequest,
                                          JwtAuthenticationToken principal) {
        UUID recipientId = UUID.fromString(principal.getName());
        DndResponse response = dndService.setByRecipient(recipientId, dndRequest);
        return ResponseEntity.ok(response);
    }
}
