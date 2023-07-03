package ru.altagroup.notificationcenter.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.altagroup.notificationcenter.dto.DndRequest;
import ru.altagroup.notificationcenter.dto.DndResponse;
import ru.altagroup.notificationcenter.entities.Dnd;
import ru.altagroup.notificationcenter.exceptions.NotFoundException;
import ru.altagroup.notificationcenter.repositories.DndRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DndService {

    private final DndRepository dndRepository;

    public DndResponse getByRecipient(UUID recipientId) {
        Dnd dnd = dndRepository.findByRecipient_Id(recipientId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с идентификатором: " + recipientId));

        return new DndResponse(recipientId, dnd.getIsActive(), dnd.getStartTime(), dnd.getEndTime(), dnd.getZoneId());
    }

    public DndResponse setByRecipient(UUID recipientId, DndRequest dndRequest) {
        Dnd dnd = dndRepository.findByRecipient_Id(recipientId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с идентификатором: " + recipientId));
        dnd.setIsActive(dndRequest.isActive());
        dnd.setZoneId(dndRequest.getZoneId().toString());
        dnd.setStartTime(dndRequest.getBegin());
        dnd.setEndTime(dndRequest.getEnd());
        Dnd save = dndRepository.save(dnd);
        return new DndResponse(recipientId, save.getIsActive(), save.getStartTime(), save.getEndTime(), dnd.getZoneId());
    }
}
