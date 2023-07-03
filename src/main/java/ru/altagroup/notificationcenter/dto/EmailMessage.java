package ru.altagroup.notificationcenter.dto;

import lombok.Builder;
import lombok.Data;
import ru.altagroup.notificationcenter.entities.MessageType;

@Data
@Builder
public class EmailMessage {
    private String timestamp;
    private String code;
    private String description;
    private String fullDescription;
    private String recommendations;
    private String stationName;
    private MessageType type;
}
