package ru.altagroup.notificationcenter.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.altagroup.notificationcenter.entities.Notice;
import ru.altagroup.notificationcenter.entities.NoticeSetting;

import java.util.Optional;
import java.util.UUID;

public interface NoticeSettingRepository extends JpaRepository<NoticeSetting, UUID> {
    Optional<NoticeSetting> findByRecipient_IdAndNotice(UUID id, Notice notice);
}
