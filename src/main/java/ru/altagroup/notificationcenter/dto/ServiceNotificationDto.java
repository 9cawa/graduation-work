package ru.altagroup.notificationcenter.dto;

import lombok.Builder;
import lombok.Data;
import ru.altagroup.notificationcenter.entities.Notice;
import ru.altagroup.notificationcenter.entities.NoticeFrequency;

import java.util.UUID;

@Data
@Builder
public class ServiceNotificationDto {
    private UUID id;
    private UUID recipientId;
    private Notice name;
    private Boolean notifyByEmail;
    private Boolean notifyBySms;
    private NoticeFrequency frequency;
}
