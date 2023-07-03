package ru.altagroup.notificationcenter.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.altacloud.v2.avro.StationNotification;
import ru.altacloud.v2.avro.UserNotification;
import ru.altagroup.notificationcenter.handlers.NotificationEventHandler;

import java.util.function.Consumer;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class NotificationStreamConfiguration {

    private final NotificationEventHandler eventHandler;

    @Bean
    public Consumer<Message<StationNotification>> consumeStationNotification() {
        return message -> {
            StationNotification event = message.getPayload();
            try {
                eventHandler.handle(event);
            } catch (IllegalArgumentException e) {
                log.error("Receive unknown event: {}", event);
            }
        };
    }

    @Bean
    public Consumer<Message<UserNotification>> consumeUserNotification() {
        return message -> {
            UserNotification event = message.getPayload();
            try {
                eventHandler.handle(event);
            } catch (IllegalArgumentException e) {
                log.error("Receive unknown event: {}", event);
            }
        };
    }
}
