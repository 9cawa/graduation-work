package ru.altagroup.notificationcenter.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.altagroup.notificationcenter.dto.MessageDto;
import ru.altagroup.notificationcenter.entities.Notice;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.events.NotificationEvent;
import ru.altagroup.notificationcenter.exceptions.NotFoundException;
import ru.altagroup.notificationcenter.factory.VerificationCodeMessageFactory;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;
import ru.altagroup.notificationcenter.services.MessageGateway;

@Component
@RequiredArgsConstructor
public class VerificationCodeNotificationStrategy implements NotificationStrategy {

    private final VerificationCodeMessageFactory verificationCodeMessageFactory;
    private final RecipientRepository recipientRepository;
    private final MessageGateway messageGateway;

    @Override
    public void notify(NotificationEvent notification) {
        notifyBySms(notification);
    }

    public void notifyBySms(NotificationEvent event) {
        Recipient recipient = recipientRepository.findById(event.getTargetRecipientId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с id " + event.getTargetRecipientId()));
        NotificationEvent.Message message = event.getMessage();
        String smsText = verificationCodeMessageFactory.createSmsText(event.getMessage().getText());
        MessageDto messageDto = MessageDto.builder().id(event.getId()).eventType(message.getEvent()).text(smsText).build();
        messageGateway.sendSms(messageDto, recipient, Notice.EMPTY);
    }
}
