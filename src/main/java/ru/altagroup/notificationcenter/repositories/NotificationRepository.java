package ru.altagroup.notificationcenter.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.altagroup.notificationcenter.entities.Channel;
import ru.altagroup.notificationcenter.entities.EventType;
import ru.altagroup.notificationcenter.entities.Notice;
import ru.altagroup.notificationcenter.entities.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findFirstByRecipient_IdAndNoticeAndChannelOrderByTimestampDesc(UUID recipientId, Notice notice, Channel channel);
    Optional<List<Notification>> findFirst3ByRecipient_IdAndEventOrderByTimestampDesc(UUID recipientId, EventType event);
}
