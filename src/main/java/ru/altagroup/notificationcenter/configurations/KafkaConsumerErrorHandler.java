package ru.altagroup.notificationcenter.configurations;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumerErrorHandler implements CommonErrorHandler {

    @Override
    public boolean handleOne(Exception exception, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, MessageListenerContainer container) {
        log.error("Cannot read message from topic: {} and offset: {}. Message: {}",
                record.topic(), record.offset(), exception.getMessage());
        return Boolean.TRUE;
    }
}
