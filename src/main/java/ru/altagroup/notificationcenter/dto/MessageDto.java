package ru.altagroup.notificationcenter.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.altagroup.notificationcenter.entities.EventType;

import java.util.UUID;

@Builder
@Getter
@Setter
public class MessageDto {
    private UUID id;
    private EventType eventType;
    private String text;
}
