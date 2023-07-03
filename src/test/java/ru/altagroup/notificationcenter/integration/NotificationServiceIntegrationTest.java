package ru.altagroup.notificationcenter.integration;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import ru.altagroup.notificationcenter.entities.*;
import ru.altagroup.notificationcenter.events.StationNotificationEvent;
import ru.altagroup.notificationcenter.repositories.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest(properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:postgresql:14.5-alpine:///test?TC_DAEMON=true",
        "spring.mail.host=smtp.yandex.ru",
        "spring.mail.port=465",
        "spring.mail.username=noreply@alta-cloud.ru",
        "spring.mail.password=zlxnfsfpjunczbvi",
        "spring.mail.properties.mail.smtp.auth=true",
        "spring.mail.properties.mail.smtp.socketFactory.port=465",
        "spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory"
})
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:19092"})
public class NotificationServiceIntegrationTest {

    @Value("${spring.kafka.topics.notifications.stations}") private String topic;
    @Autowired private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    @Autowired private RecipientRepository recipientRepository;
    @Autowired private StationRepository stationRepository;
    @Autowired private DndRepository dndRepository;
    @Autowired private NoticeSettingRepository noticeSettingRepository;
    @Autowired private NotificationRepository notificationRepository;

    private final UUID stationId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();

    @BeforeTestClass
    public void beforeTest() {
        kafkaListenerEndpointRegistry.getListenerContainers().forEach(
                messageListenerContainer -> ContainerTestUtils.waitForAssignment(messageListenerContainer, 1)
        );
    }

    @BeforeEach
    public void beforeEach() {
        ConcurrentMessageListenerContainer<?, ?> listenerContainer =
                (ConcurrentMessageListenerContainer<?, ?>) kafkaListenerEndpointRegistry.getListenerContainer("stationNotificationEventListener");
        listenerContainer.start();
    }

    @BeforeEach
    public void setup() {
        Recipient recipient = new Recipient();
        recipient.setId(ownerId);
        recipient.setFullName("Nikolai Pletnev");
        recipient.setType(RecipientType.INDIVIDUAL_PERSON);
        recipient.setEmail("n.pletnev@alta-group.ru");
        recipient.setPhone("79629132587");
        Recipient savedRecipient = recipientRepository.save(recipient);

        Station station = new Station();
        station.setId(stationId);
        station.setName("name1");
        station.setRecipient(savedRecipient);
        stationRepository.save(station);

        Dnd dnd = new Dnd();
        dnd.setActive(false);
        dnd.setRecipient(savedRecipient);
        dndRepository.save(dnd);

        NoticeSetting noticeSetting = new NoticeSetting(Notice.SYSTEM);
        noticeSetting.setByEmail(true);
        noticeSetting.setBySms(false);
        noticeSetting.setFrequency(NoticeFrequency.HOURLY);
        noticeSetting.setRecipient(savedRecipient);
        noticeSettingRepository.save(noticeSetting);

        savedRecipient.setDnd(dnd);
        savedRecipient.getStations().add(station);
        savedRecipient.getNoticeSettings().add(noticeSetting);
        recipientRepository.save(savedRecipient);
    }

    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        return props;
    }

    @Test
    @Ignore
    public void testConsumeDealerSubscriptionEvent() throws InterruptedException {
        KafkaProducer<String, StationNotificationEvent> producer = new KafkaProducer<>(producerConfigs());

        StationNotificationEvent notificationEvent = new StationNotificationEvent();
        notificationEvent.setId(UUID.randomUUID());
        notificationEvent.setStationId(stationId);
        notificationEvent.setTimestamp(Instant.now().toEpochMilli());
        StationNotificationEvent.Message message = new StationNotificationEvent.Message();
        message.setEvent(EventType.DEALER_SUBSCRIPTION);
        message.setType(MessageType.SUBSCRIPTION_NOTIFICATION_USER);
        Map<String, String> advanced = new HashMap<>() {{
            put("DEALER_NAME", "TEST_DEALER_NAME");
        }};
        message.setAdvanced(advanced);
        notificationEvent.setMessage(message);

        ProducerRecord<String, StationNotificationEvent> record = new ProducerRecord<>(topic, notificationEvent);
        TimeUnit.SECONDS.sleep(5);
        producer.send(record);
        TimeUnit.SECONDS.sleep(5);
        List<Notification> all = notificationRepository.findAll();
        Assertions.assertEquals(1, all.size());
    }
}
