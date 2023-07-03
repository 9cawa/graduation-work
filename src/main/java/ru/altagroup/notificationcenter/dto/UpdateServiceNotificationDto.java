package ru.altagroup.notificationcenter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.altagroup.notificationcenter.dto.validators.ValueOfEnum;
import ru.altagroup.notificationcenter.entities.Notice;
import ru.altagroup.notificationcenter.entities.NoticeFrequency;

@Data
@Builder
public class UpdateServiceNotificationDto {
    @ValueOfEnum(enumClass = Notice.class)
    private String name;
    @NotNull
    private Boolean notifyByEmail;
    @NotNull
    private Boolean notifyBySms;
    @ValueOfEnum(enumClass = NoticeFrequency.class)
    private String frequency;
}
