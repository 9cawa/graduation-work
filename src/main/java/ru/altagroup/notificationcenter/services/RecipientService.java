package ru.altagroup.notificationcenter.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.altagroup.notificationcenter.events.RecipientEventType;
import ru.altagroup.notificationcenter.events.UserEvent;
import ru.altagroup.notificationcenter.handlers.RecipientCreateEventHandler;
import ru.altagroup.notificationcenter.handlers.RecipientDeleteEventHandler;
import ru.altagroup.notificationcenter.handlers.RecipientEventHandler;
import ru.altagroup.notificationcenter.repositories.DndRepository;
import ru.altagroup.notificationcenter.repositories.NoticeSettingRepository;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;
import ru.altagroup.notificationcenter.repositories.StationRepository;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final DndRepository dndRepository;
    private final NoticeSettingRepository noticeSettingRepository;

    private final StationRepository stationRepository;

    @PostConstruct
    private Map<RecipientEventType, RecipientEventHandler> getHandlers() {
        return new HashMap<>(){{
            put(RecipientEventType.CREATE, new RecipientCreateEventHandler(recipientRepository, dndRepository, noticeSettingRepository));
            put(RecipientEventType.DELETE, new RecipientDeleteEventHandler(recipientRepository, stationRepository, dndRepository, noticeSettingRepository));
        }};
    }

    @KafkaListener(topics = "${spring.kafka.topics.cdc.users}", containerFactory = "userEventContainerFactory")
    @Transactional
    public void listenUserEvent(@Payload @Valid UserEvent event) {
        try {
            RecipientEventType recipientEventType = RecipientEventType.valueOf(event.getEvent());
            RecipientEventHandler recipientEventHandler = getHandlers().get(recipientEventType);
            recipientEventHandler.handle(event);
        } catch (IllegalArgumentException e) {
            log.error("Receive unknown event: {}", event);
        }
    }

    @KafkaListener(topics = "${spring.kafka.topics.cdc.dealers}", containerFactory = "userEventContainerFactory")
    public void listenDealerEvent(@Payload @Valid UserEvent event) {
        try {
            RecipientEventType recipientEventType = RecipientEventType.valueOf(event.getEvent());
            RecipientEventHandler recipientEventHandler = getHandlers().get(recipientEventType);
            recipientEventHandler.handle(event);
        } catch (IllegalArgumentException e) {
            log.error("Receive unknown event: {}", event);
        }
    }
}
