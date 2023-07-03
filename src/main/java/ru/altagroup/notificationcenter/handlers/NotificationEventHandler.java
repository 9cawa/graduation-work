package ru.altagroup.notificationcenter.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.altagroup.notificationcenter.entities.EventType;
import ru.altagroup.notificationcenter.events.NotificationEvent;
import ru.altagroup.notificationcenter.events.StationNotificationEvent;
import ru.altagroup.notificationcenter.events.UserNotificationEvent;

import javax.mail.MessagingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventHandler {

    private final NotificationStrategySelector notificationStrategySelector;

    public void handle(StationNotificationEvent event) throws MessagingException {
        StationNotificationEvent.Message eventMessage = event.getMessage();
        EventType eventType = eventMessage.getEvent();
        NotificationStrategy notificationStrategy = notificationStrategySelector.selectStrategyByEventType(eventType);

        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setId(event.getId());
        notificationEvent.setTargetRecipientId(event.getStationId());
        notificationEvent.setTimestamp(event.getTimestamp());

        NotificationEvent.Message message = new NotificationEvent.Message();
        message.setEvent(eventMessage.getEvent());
        message.setAdvanced(eventMessage.getAdvanced());
        message.setText(eventMessage.getCode());
        message.setType(eventMessage.getType());

        notificationEvent.setMessage(message);

        notificationStrategy.notify(notificationEvent);
    }

    public void handle(UserNotificationEvent event) throws MessagingException {
        UserNotificationEvent.Message eventMessage = event.getMessage();
        EventType eventType = eventMessage.getEvent();
        NotificationStrategy notificationStrategy = notificationStrategySelector.selectStrategyByEventType(eventType);

        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setId(event.getId());
        notificationEvent.setTargetRecipientId(event.getUserId());
        notificationEvent.setTimestamp(event.getTimestamp());

        NotificationEvent.Message message = new NotificationEvent.Message();
        message.setEvent(eventMessage.getEvent());
        message.setText(eventMessage.getText());
        message.setType(eventMessage.getType());

        notificationEvent.setMessage(message);
        notificationStrategy.notify(notificationEvent);
    }
}
