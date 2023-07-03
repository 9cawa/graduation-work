package ru.altagroup.notificationcenter.configurations;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.altagroup.notificationcenter.events.StationBindingEvent;
import ru.altagroup.notificationcenter.events.StationNotificationEvent;
import ru.altagroup.notificationcenter.events.UserEvent;
import ru.altagroup.notificationcenter.events.UserNotificationEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class ConsumerConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value("${spring.kafka.consumer.client-id}")
    private String clientId;

    @Bean
    public Map<String, Object> properties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return props;
    }

    @Bean("userEventContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent> userContainerFactory() {
        StringDeserializer keyDeserializer = new StringDeserializer();
        JsonDeserializer<UserEvent> valueDeserializer = new JsonDeserializer<>(UserEvent.class).ignoreTypeHeaders();
        ErrorHandlingDeserializer<UserEvent> valueErrorHandlingDeserializer = new ErrorHandlingDeserializer<>(valueDeserializer);
        DefaultKafkaConsumerFactory<String, UserEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(properties(), keyDeserializer, valueErrorHandlingDeserializer);
        ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(new KafkaConsumerErrorHandler());
        return factory;
    }

    @Bean("stationBindingEventContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, StationBindingEvent> stationContainerFactory() {
        StringDeserializer keyDeserializer = new StringDeserializer();
        JsonDeserializer<StationBindingEvent> valueDeserializer = new JsonDeserializer<>(StationBindingEvent.class).ignoreTypeHeaders();
        ErrorHandlingDeserializer<StationBindingEvent> valueErrorHandlingDeserializer = new ErrorHandlingDeserializer<>(valueDeserializer);
        DefaultKafkaConsumerFactory<String, StationBindingEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(properties(), keyDeserializer, valueErrorHandlingDeserializer);
        ConcurrentKafkaListenerContainerFactory<String, StationBindingEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(new KafkaConsumerErrorHandler());
        return factory;
    }

    @Bean("stationNotificationEventContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, StationNotificationEvent> stationNotificationContainerFactory() {
        StringDeserializer keyDeserializer = new StringDeserializer();
        JsonDeserializer<StationNotificationEvent> valueDeserializer = new JsonDeserializer<>(StationNotificationEvent.class).ignoreTypeHeaders();
        ErrorHandlingDeserializer<StationNotificationEvent> valueErrorHandlingDeserializer = new ErrorHandlingDeserializer<>(valueDeserializer);
        DefaultKafkaConsumerFactory<String, StationNotificationEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(properties(), keyDeserializer, valueErrorHandlingDeserializer);
        ConcurrentKafkaListenerContainerFactory<String, StationNotificationEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(new KafkaConsumerErrorHandler());
        return factory;
    }

    @Bean("userNotificationEventContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, UserNotificationEvent> userNotificationContainerFactory() {
        StringDeserializer keyDeserializer = new StringDeserializer();
        JsonDeserializer<UserNotificationEvent> valueDeserializer = new JsonDeserializer<>(UserNotificationEvent.class).ignoreTypeHeaders();
        ErrorHandlingDeserializer<UserNotificationEvent> valueErrorHandlingDeserializer = new ErrorHandlingDeserializer<>(valueDeserializer);
        DefaultKafkaConsumerFactory<String, UserNotificationEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(properties(), keyDeserializer, valueErrorHandlingDeserializer);
        ConcurrentKafkaListenerContainerFactory<String, UserNotificationEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(new KafkaConsumerErrorHandler());
        return factory;
    }
}
