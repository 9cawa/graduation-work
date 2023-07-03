package ru.altagroup.notificationcenter.events;

import lombok.Data;
import ru.altagroup.notificationcenter.entities.EventType;
import ru.altagroup.notificationcenter.entities.MessageType;

import java.util.Map;
import java.util.UUID;

@Data
public class NotificationEvent {
    private UUID id;
    private Long timestamp;
    private UUID targetRecipientId;
    private Message message;

    @Data
    public static class Message {
        private MessageType type;
        private EventType event;
        private String text;
        private Map<String, String> advanced;
    }
}
