package ru.altagroup.notificationcenter.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.altagroup.notificationcenter.dto.ServiceNotificationDto;
import ru.altagroup.notificationcenter.dto.UpdateServiceNotificationDto;
import ru.altagroup.notificationcenter.entities.Notice;
import ru.altagroup.notificationcenter.entities.NoticeFrequency;
import ru.altagroup.notificationcenter.entities.NoticeSetting;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.events.StationNotificationEvent;
import ru.altagroup.notificationcenter.events.UserNotificationEvent;
import ru.altagroup.notificationcenter.exceptions.NotFoundException;
import ru.altagroup.notificationcenter.handlers.NotificationEventHandler;
import ru.altagroup.notificationcenter.repositories.NoticeSettingRepository;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;

import javax.mail.MessagingException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationEventHandler eventHandler;
    private final RecipientRepository recipientRepository;
    private final NoticeSettingRepository noticeSettingRepository;

    @KafkaListener(id = "stationNotificationEventListener", topics = "${spring.kafka.topics.notifications.stations}", containerFactory = "stationNotificationEventContainerFactory")
    public void notifyStationEvent(StationNotificationEvent event) throws MessagingException {
        try {
            eventHandler.handle(event);
        } catch (IllegalArgumentException e) {
            log.error("Receive unknown event: {}", event);
        }
    }

    @KafkaListener(topics = "${spring.kafka.topics.notifications.users}", containerFactory = "userNotificationEventContainerFactory")
    public void notifyUserEvent(UserNotificationEvent event) throws MessagingException {
        try {
            eventHandler.handle(event);
        } catch (IllegalArgumentException e) {
            log.error("Receive unknown event: {}", event);
        }
    }

    public List<ServiceNotificationDto> getNotificationsSettings(UUID recipientId) {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с идентификатором: " + recipientId));
        List<NoticeSetting> noticeSettings = recipient.getNoticeSettings();
        return noticeSettings.stream().map(this::toServiceNotificationDto).collect(Collectors.toList());
    }

    public ServiceNotificationDto updateNotificationSettings(UUID recipientId, UpdateServiceNotificationDto update) {
        Notice notice = Notice.valueOf(update.getName());
        NoticeSetting noticeSetting = noticeSettingRepository.findByRecipient_IdAndNotice(recipientId, notice)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с идентификатором: " + recipientId));

        noticeSetting.setByEmail(update.getNotifyByEmail());
        noticeSetting.setBySms(update.getNotifyBySms());
        noticeSetting.setFrequency(NoticeFrequency.valueOf(update.getFrequency()));
        NoticeSetting save = noticeSettingRepository.save(noticeSetting);
        return toServiceNotificationDto(save);
    }

    private ServiceNotificationDto toServiceNotificationDto(NoticeSetting noticeSetting) {
        return ServiceNotificationDto.builder()
                .id(noticeSetting.getId())
                .recipientId(noticeSetting.getRecipient().getId())
                .name(noticeSetting.getNotice())
                .notifyByEmail(noticeSetting.getByEmail())
                .notifyBySms(noticeSetting.getBySms())
                .frequency(noticeSetting.getFrequency())
                .build();
    }

}
