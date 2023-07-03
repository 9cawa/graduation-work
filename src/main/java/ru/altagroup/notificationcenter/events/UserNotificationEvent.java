package ru.altagroup.notificationcenter.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import ru.altagroup.notificationcenter.entities.EventType;
import ru.altagroup.notificationcenter.entities.MessageType;

import java.util.UUID;

@Data
public class UserNotificationEvent {
    private UUID id;
    private Long timestamp;
    private UUID userId;
    private String email;
    private String phone;
    private Message message;

    @Getter
    @AllArgsConstructor
    public static class Message {
        private final MessageType type;
        private final EventType event;
        private String text;
    }
}
