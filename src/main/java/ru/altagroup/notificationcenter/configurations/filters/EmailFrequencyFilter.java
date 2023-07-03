package ru.altagroup.notificationcenter.configurations.filters;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import ru.altagroup.notificationcenter.entities.*;
import ru.altagroup.notificationcenter.repositories.NoticeSettingRepository;
import ru.altagroup.notificationcenter.repositories.NotificationRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmailFrequencyFilter implements ChainFilter {

    private ChainFilter chain;
    private final NotificationRepository notificationRepository;
    private final NoticeSettingRepository noticeSettingRepository;

    @Override
    public void setNextChain(ChainFilter nextChain) {
        this.chain = nextChain;
    }

    @Override
    public boolean filter(Message<?> message) {
        Recipient recipient = message.getHeaders().get("recipient", Recipient.class);
        Notice notice = message.getHeaders().get("notice", Notice.class);
        assert recipient != null;
        NoticeFrequency frequency = getFrequency(recipient.getId(), notice);
        Optional<Notification> lastNotification = notificationRepository.findFirstByRecipient_IdAndNoticeAndChannelOrderByTimestampDesc(recipient.getId(), notice, Channel.EMAIL);
        if (lastNotification.isEmpty()) return true;
        ZoneId userZoneId = ZoneId.of(recipient.getDnd().getZoneId());
        LocalDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime lastTime = Instant.ofEpochMilli(lastNotification.get().getTimestamp()).atZone(userZoneId).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().truncatedTo(ChronoUnit.MINUTES);
        if (chain == null || !lastTime.plus(frequency.getChronoUnit().getDuration()).isBefore(now)) return false;
        return chain.filter(message);
    }

    private NoticeFrequency getFrequency(UUID recipientId, Notice notice) {
        Optional<NoticeSetting> optionalNoticeSetting = noticeSettingRepository.findByRecipient_IdAndNotice(recipientId, notice);
        if (optionalNoticeSetting.isPresent()) return optionalNoticeSetting.get().getFrequency();
        return NoticeFrequency.NEVER;
    }
}
