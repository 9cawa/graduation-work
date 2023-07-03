package ru.altagroup.notificationcenter.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.altagroup.notificationcenter.dto.MessageDto;
import ru.altagroup.notificationcenter.entities.EventType;
import ru.altagroup.notificationcenter.entities.Notice;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.entities.Station;
import ru.altagroup.notificationcenter.events.NotificationEvent;
import ru.altagroup.notificationcenter.exceptions.NotFoundException;
import ru.altagroup.notificationcenter.factory.HealthResetMessageFactory;
import ru.altagroup.notificationcenter.repositories.StationRepository;
import ru.altagroup.notificationcenter.services.MessageGateway;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HealthResetNotificationStrategy implements NotificationStrategy {

    private final HealthResetMessageFactory healthResetMessageFactory;
    private final StationRepository stationRepository;
    private final MessageGateway messageGateway;

    @Override
    public void notify(NotificationEvent notification) {
        notifyByEmail(notification);
        notifyBySms(notification);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void notifyBySms(NotificationEvent event) {
        Station station = stationRepository.findById(event.getTargetRecipientId())
                .orElseThrow(() -> new NotFoundException("Станция не найдена с идентификатором " + event.getTargetRecipientId()));
        Recipient recipient = station.getRecipient();
        String smsText = healthResetMessageFactory.createSmsText(station.getName());
        NotificationEvent.Message msg = event.getMessage();
        MessageDto message = createMessage(msg.getEvent(), event.getId(), smsText);
        messageGateway.sendSms(message, recipient, Notice.SYSTEM);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void notifyByEmail(NotificationEvent event) {
        Station station = stationRepository.findById(event.getTargetRecipientId())
                .orElseThrow(() -> new NotFoundException("Станция не найдена с идентификатором " + event.getTargetRecipientId()));
        Recipient recipient = station.getRecipient();
        String htmlEmailText = healthResetMessageFactory.createHtmlEmailText(event.getTimestamp(), station.getName());
        NotificationEvent.Message msg = event.getMessage();
        MessageDto message = createMessage(msg.getEvent(), event.getId(), htmlEmailText);
        messageGateway.sendEmail(message, recipient, Notice.SYSTEM);
    }

    private MessageDto createMessage(EventType eventType, UUID eventId, String text) {
        return MessageDto.builder().id(eventId).eventType(eventType).text(text).build();
    }
}
