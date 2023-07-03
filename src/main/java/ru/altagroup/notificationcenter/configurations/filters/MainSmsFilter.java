package ru.altagroup.notificationcenter.configurations.filters;

import lombok.AllArgsConstructor;
import org.springframework.integration.annotation.Filter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import ru.altagroup.notificationcenter.dto.MessageDto;
import ru.altagroup.notificationcenter.entities.EventType;
import ru.altagroup.notificationcenter.entities.Notification;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.repositories.NotificationRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Component
public class MainSmsFilter {

    private final NotificationRepository notificationRepository;
    private final DndFilter dndFilter;
    private final SmsMessageFilter smsMessageFilter;
    private final SmsFrequencyFilter smsFrequencyFilter;

    @Filter
    public boolean accept(Message<?> message) {
        MessageDto messageDto = (MessageDto) message.getPayload();
        EventType type = messageDto.getEventType();
        if (type.equals(EventType.VERIFICATION_CODE)) return isMoreThanThreeVerificationCode(message);

        ChainFilter chainFilter = getChainFilter();
        return chainFilter.filter(message);
    }

    private ChainFilter getChainFilter() {
        dndFilter.setNextChain(smsMessageFilter);
        smsMessageFilter.setNextChain(smsFrequencyFilter);
        return dndFilter;
    }

    //true -> отправлять, false -> не отправлять
    private boolean isMoreThanThreeVerificationCode(Message<?> message) {
        Recipient recipient = message.getHeaders().get("recipient", Recipient.class);
        assert recipient != null;
        Optional<List<Notification>> verCodeNotifications = notificationRepository.findFirst3ByRecipient_IdAndEventOrderByTimestampDesc(recipient.getId(), EventType.VERIFICATION_CODE);
        assert verCodeNotifications.isPresent();
        if (verCodeNotifications.get().size() < 3) return true;

        LocalDate now = LocalDate.now();
        for (Notification notification : verCodeNotifications.get()) {
            LocalDate notificationDate = Instant.ofEpochMilli(notification.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDate();
            if (notificationDate.isBefore(now)) return true;
        }
        return false;
    }
}
