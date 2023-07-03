package ru.altagroup.notificationcenter.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.altagroup.notificationcenter.dto.MessageDto;
import ru.altagroup.notificationcenter.entities.*;
import ru.altagroup.notificationcenter.repositories.*;
import ru.altagroup.notificationcenter.services.MessageGateway;

import java.util.UUID;

@SpringBootTest(properties = {
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
public class EmailIntegrationTest {

    @Autowired private MessageGateway gateway;
    @Autowired private StationRepository stationRepository;
    @Autowired private RecipientRepository recipientRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private DndRepository dndRepository;
    @Autowired private NoticeSettingRepository noticeSettingRepository;

    private final UUID stationId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();

    @BeforeEach
    public void setup() {
        Recipient recipient = new Recipient();
        recipient.setId(ownerId);
        recipient.setFullName("Nikolai Pletnev");
        recipient.setType(RecipientType.INDIVIDUAL_PERSON);
        recipient.setEmail("test-alta-cloud@yandex.ru");
//        recipient.setEmail("n.pletnev@alta-group.ru");
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

        NoticeSetting noticeSetting = new NoticeSetting(Notice.EMPTY);
        noticeSetting.setByEmail(true);
        noticeSetting.setBySms(true);
        noticeSetting.setFrequency(NoticeFrequency.HOURLY);
        noticeSetting.setRecipient(savedRecipient);
        noticeSettingRepository.save(noticeSetting);

        savedRecipient.setDnd(dnd);
        savedRecipient.getStations().add(station);
        savedRecipient.getNoticeSettings().add(noticeSetting);
        recipientRepository.save(savedRecipient);
    }

    @Test
    @Transactional
    public void testSendEmail() {
        Recipient recipient = recipientRepository.findById(ownerId).get();
        Station station = recipient.getStations().stream().findFirst().get();
        MessageDto messageDto = MessageDto.builder()
                .id(UUID.randomUUID())
                .text("EMAIL FROM INTEGRATION TEST")
                .eventType(EventType.ERROR)
                .build();
        gateway.sendEmail(messageDto, recipient, Notice.EMPTY);
        Notification notification = notificationRepository.findAll().get(0);
        Assertions.assertEquals(notification.getRecipient(), recipient);
        Assertions.assertEquals(notification.getChannel(), Channel.EMAIL);
        Assertions.assertEquals(notification.isDelivered(), Boolean.TRUE);
    }
}
