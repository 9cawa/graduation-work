package ru.altagroup.notificationcenter.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.altacloud.v2.avro.UserEvent;
import ru.altacloud.v2.avro.UserEventType;
import ru.altagroup.notificationcenter.handlers.RecipientCreateEventHandler;
import ru.altagroup.notificationcenter.handlers.RecipientDeleteEventHandler;
import ru.altagroup.notificationcenter.handlers.RecipientEventHandler;
import ru.altagroup.notificationcenter.repositories.DndRepository;
import ru.altagroup.notificationcenter.repositories.NoticeSettingRepository;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;
import ru.altagroup.notificationcenter.repositories.StationRepository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RecipientStreamConfiguration {

    private final RecipientRepository recipientRepository;
    private final DndRepository dndRepository;
    private final NoticeSettingRepository noticeSettingRepository;
    private final StationRepository stationRepository;

    @PostConstruct
    private Map<UserEventType, RecipientEventHandler> getHandlers() {
        return new HashMap<>(){{
            put(UserEventType.CREATE, new RecipientCreateEventHandler(recipientRepository, dndRepository, noticeSettingRepository));
            put(UserEventType.DELETE, new RecipientDeleteEventHandler(recipientRepository, stationRepository, dndRepository, noticeSettingRepository));
        }};
    }

    @Bean
    public Consumer<Message<UserEvent>> consumeUserEvent() {
        return message -> {
            try {
                UserEvent event = message.getPayload();
                UserEventType recipientEventType = event.getEvent();
                RecipientEventHandler recipientEventHandler = getHandlers().get(recipientEventType);
                recipientEventHandler.handle(event);
            } catch (IllegalArgumentException e) {
                log.error("Receive unknown event: {}", message.getPayload());
            }
        };
    }
}
