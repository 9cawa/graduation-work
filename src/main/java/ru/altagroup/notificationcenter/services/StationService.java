package ru.altagroup.notificationcenter.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.altagroup.notificationcenter.events.BindingEventType;
import ru.altagroup.notificationcenter.events.StationBindingEvent;
import ru.altagroup.notificationcenter.handlers.StationBindEventHandler;
import ru.altagroup.notificationcenter.handlers.StationEventHandler;
import ru.altagroup.notificationcenter.handlers.StationUnbindEventHandler;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;
import ru.altagroup.notificationcenter.repositories.StationRepository;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationService {

    private final StationRepository stationRepository;
    private final RecipientRepository recipientRepository;

    @PostConstruct
    private Map<BindingEventType, StationEventHandler> getHandlers() {
        return new HashMap<>(){{
            put(BindingEventType.BIND, new StationBindEventHandler(stationRepository, recipientRepository));
            put(BindingEventType.UNBIND, new StationUnbindEventHandler(stationRepository));
        }};
    }

    @KafkaListener(topics = "${spring.kafka.topics.binding}", containerFactory = "stationBindingEventContainerFactory")
    public void listenStationEvent(@Payload @Valid StationBindingEvent event) {
        try {
            BindingEventType bindingEventType = BindingEventType.valueOf(event.getEvent());
            StationEventHandler stationEventHandler = getHandlers().get(bindingEventType);
            stationEventHandler.handle(event);
        } catch (IllegalArgumentException e) {
            log.error("Receive unknown event: {}", event);
        }
    }
}
