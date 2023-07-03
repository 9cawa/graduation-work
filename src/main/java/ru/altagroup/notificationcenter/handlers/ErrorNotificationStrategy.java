package ru.altagroup.notificationcenter.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.altagroup.notificationcenter.dto.MessageDto;
import ru.altagroup.notificationcenter.entities.*;
import ru.altagroup.notificationcenter.events.NotificationEvent;
import ru.altagroup.notificationcenter.exceptions.NotFoundException;
import ru.altagroup.notificationcenter.factory.ErrorMessageFactory;
import ru.altagroup.notificationcenter.repositories.StationRepository;
import ru.altagroup.notificationcenter.services.MessageGateway;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class ErrorNotificationStrategy implements NotificationStrategy {

    private final ErrorMessageFactory errorMessageFactory;
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
        NotificationEvent.Message message = event.getMessage();
        assert message.getText() != null;
        String smsText = errorMessageFactory.createSmsText(station.getName(), message.getText());
        MessageDto messageDto = createMessage(message.getEvent(), event.getId(), smsText);
        Notice service = errorMessageFactory.retrieveErrorCode(message.getText()).getService();
        messageGateway.sendSms(messageDto, recipient, service);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void notifyByEmail(NotificationEvent event) {
        Station station = stationRepository.findById(event.getTargetRecipientId())
                .orElseThrow(() -> new NotFoundException("Станция не найдена с идентификатором " + event.getTargetRecipientId()));
        Recipient recipient = station.getRecipient();
        NotificationEvent.Message message = event.getMessage();
        assert message.getText() != null;
        ErrorCode errorCode = errorMessageFactory.retrieveErrorCode(message.getText());
        String emailText = errorMessageFactory.createHtmlEmailText(event.getTimestamp(), station.getName(), message.getText());
        MessageDto messageDto = createMessage(message.getEvent(), event.getId(), emailText);
        Notice service = errorCode.getService();
        messageGateway.sendEmail(messageDto, recipient, service);
    }

    private MessageDto createMessage(EventType eventType, UUID eventId, String text) {
        return MessageDto.builder().id(eventId).eventType(eventType).text(text).build();
    }
}
