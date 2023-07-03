package ru.altagroup.notificationcenter.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.kafka.common.protocol.types.Field;
import ru.altagroup.notificationcenter.entities.EventType;
import ru.altagroup.notificationcenter.entities.MessageType;

import java.util.Map;
import java.util.UUID;

@Data
public class StationNotificationEvent {

    private UUID id;

    private Long timestamp;

    @JsonProperty("station_id")
    private UUID stationId;

    private Message message;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private MessageType type;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String code;

        private EventType event;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Map<String, String> advanced;

        public Message(MessageType type, String code, EventType event) {
            this.type = type;
            this.code = code;
            this.event = event;
        }
    }
}
