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
import ru.altagroup.notificationcenter.factory.DealerSubscriptionMessageFactory;
import ru.altagroup.notificationcenter.factory.DealerUnsubscriptionMessageFactory;
import ru.altagroup.notificationcenter.repositories.StationRepository;
import ru.altagroup.notificationcenter.services.MessageGateway;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealerSubscriptionNotificationStrategy implements NotificationStrategy {

    private final DealerSubscriptionMessageFactory dealerSubscriptionMessageFactory;
    private final DealerUnsubscriptionMessageFactory dealerUnsubscriptionMessageFactory;
    private final StationRepository stationRepository;
    private final MessageGateway messageGateway;

    @Override
    public void notify(NotificationEvent notification) {
        notifyBySms(notification);
        notifyByEmail(notification);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void notifyBySms(NotificationEvent event) {
        Station station = findStation(event.getTargetRecipientId());
        Recipient recipient = station.getRecipient();
        NotificationEvent.Message message = event.getMessage();
        Map<String, String> advanced = message.getAdvanced();
        String smsText = null;
        if (EventType.DEALER_SUBSCRIPTION.equals(message.getEvent()))
            smsText = dealerSubscriptionMessageFactory.createSmsText(station.getName(), advanced.get("DEALER_NAME"));
        if (EventType.DEALER_UNSUBSCRIPTION.equals(message.getEvent()))
            smsText = dealerUnsubscriptionMessageFactory.createSmsText(station.getName(), advanced.get("DEALER_NAME"));
        MessageDto messageDto = createMessage(message.getEvent(), event.getId(), smsText);
        messageGateway.sendSms(messageDto, recipient, Notice.SYSTEM);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void notifyByEmail(NotificationEvent event) {
        Station station = findStation(event.getTargetRecipientId());
        Recipient recipient = station.getRecipient();
        NotificationEvent.Message message = event.getMessage();
        Map<String, String> advanced = message.getAdvanced();
        String emailText = null;
        if (EventType.DEALER_SUBSCRIPTION.equals(message.getEvent()))
            emailText = dealerSubscriptionMessageFactory.createHtmlEmailText(
                    event.getTimestamp(),
                    station.getName(),
                    advanced.get("DEALER_NAME"),
                    advanced.get("DEALER_PHONE"),
                    advanced.get("DEALER_EMAIL"));
        if (EventType.DEALER_UNSUBSCRIPTION.equals(message.getEvent()))
            emailText = dealerUnsubscriptionMessageFactory.createHtmlEmailText(event.getTimestamp(), station.getName(), advanced.get("DEALER_NAME"));
        MessageDto messageDto = createMessage(message.getEvent(), event.getId(), emailText);
        messageGateway.sendEmail(messageDto, recipient, Notice.SYSTEM);
    }

    private MessageDto createMessage(EventType eventType, UUID eventId, String text) {
        return MessageDto.builder().id(eventId).eventType(eventType).text(text).build();
    }

    private Station findStation(UUID targetRecipientId) {
        return stationRepository.findById(targetRecipientId)
                .orElseThrow(() -> new NotFoundException("Станция не найдена с идентификатором " + targetRecipientId));
    }
}
