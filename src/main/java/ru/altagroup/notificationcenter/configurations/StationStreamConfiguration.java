package ru.altagroup.notificationcenter.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.altacloud.v2.avro.BindType;
import ru.altacloud.v2.avro.StationBindingEvent;
import ru.altagroup.notificationcenter.handlers.StationBindEventHandler;
import ru.altagroup.notificationcenter.handlers.StationEventHandler;
import ru.altagroup.notificationcenter.handlers.StationUnbindEventHandler;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;
import ru.altagroup.notificationcenter.repositories.StationRepository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class StationStreamConfiguration {

    private final StationRepository stationRepository;
    private final RecipientRepository recipientRepository;

    @PostConstruct
    private Map<BindType, StationEventHandler> getHandlers() {
        return new HashMap<>(){{
            put(BindType.BIND, new StationBindEventHandler(stationRepository, recipientRepository));
            put(BindType.UNBIND, new StationUnbindEventHandler(stationRepository));
        }};
    }

    @Bean
    public Consumer<Message<StationBindingEvent>> consumeStationEvent() {
        return message -> {
            try {
                StationBindingEvent event = message.getPayload();
                BindType bindingEventType = event.getEvent();
                StationEventHandler stationEventHandler = getHandlers().get(bindingEventType);
                stationEventHandler.handle(event);
            } catch (IllegalArgumentException e) {
                log.error("Receive unknown event: {}", message.getPayload());
            }
        };
    }
}
