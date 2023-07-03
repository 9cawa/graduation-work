package ru.altagroup.notificationcenter.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.altacloud.v2.avro.StationNoticeMessage;
import ru.altacloud.v2.avro.StationNotification;
import ru.altacloud.v2.avro.UserNoticeMessage;
import ru.altacloud.v2.avro.UserNotification;
import ru.altagroup.notificationcenter.entities.EventType;
import ru.altagroup.notificationcenter.entities.MessageType;
import ru.altagroup.notificationcenter.events.NotificationEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventHandler {

    private final NotificationStrategySelector notificationStrategySelector;

    public void handle(StationNotification event) {
        StationNoticeMessage eventMessage = event.getMessage();
        EventType eventType = EventType.valueOf(eventMessage.getEvent().name());
        NotificationStrategy notificationStrategy = notificationStrategySelector.selectStrategyByEventType(eventType);

        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setId(event.getId());
        notificationEvent.setTargetRecipientId(event.getStationId());
        notificationEvent.setTimestamp(event.getTimestamp());

        Map<String, String> advanced = Optional.ofNullable(eventMessage.getAdvanced())
                .orElseGet(HashMap::new).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));

        NotificationEvent.Message message = new NotificationEvent.Message();
        message.setEvent(EventType.valueOf(eventMessage.getEvent().name()));
        message.setAdvanced(advanced);
        message.setText(eventMessage.getCode().toString());
        message.setType(MessageType.valueOf(eventMessage.getType().toString()));

        notificationEvent.setMessage(message);

        notificationStrategy.notify(notificationEvent);
    }

    public void handle(UserNotification event) {
        UserNoticeMessage eventMessage = event.getMessage();
        EventType eventType = EventType.valueOf(eventMessage.getEvent().name());
        NotificationStrategy notificationStrategy = notificationStrategySelector.selectStrategyByEventType(eventType);

        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setId(event.getId());
        notificationEvent.setTargetRecipientId(event.getUserId());
        notificationEvent.setTimestamp(event.getTimestamp());

        NotificationEvent.Message message = new NotificationEvent.Message();
        message.setEvent(EventType.valueOf(eventMessage.getEvent().name()));
        message.setText(eventMessage.getText().toString());
        message.setType(MessageType.valueOf(eventMessage.getType().toString()));

        notificationEvent.setMessage(message);
        notificationStrategy.notify(notificationEvent);
    }
}
