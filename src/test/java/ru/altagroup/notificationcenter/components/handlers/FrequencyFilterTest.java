package ru.altagroup.notificationcenter.components.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.altacloud.v2.avro.StationNoticeMessage;
import ru.altacloud.v2.avro.StationNotification;
import ru.altacloud.v2.avro.StationNotificationEventType;
import ru.altacloud.v2.avro.StationNotificationMessageType;
import ru.altagroup.notificationcenter.dto.SmsClientResponse;
import ru.altagroup.notificationcenter.dto.SmsClientResult;
import ru.altagroup.notificationcenter.dto.SmsClientStatus;
import ru.altagroup.notificationcenter.entities.*;
import ru.altagroup.notificationcenter.handlers.NotificationEventHandler;
import ru.altagroup.notificationcenter.repositories.*;
import ru.altagroup.notificationcenter.services.MessageGateway;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.UUID;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

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
@Testcontainers
public class FrequencyFilterTest {
    public static MockServerContainer mockServer =
            new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.14.0"));

    @Autowired private MessageGateway messageGateway;
    @Autowired private NotificationEventHandler notificationEventHandler;
    @Autowired private StationRepository stationRepository;
    @Autowired private RecipientRepository recipientRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private DndRepository dndRepository;
    @Autowired private NoticeSettingRepository noticeSettingRepository;

    private final UUID stationId = UUID.randomUUID();
    private final UUID recipientId = UUID.randomUUID();

    @BeforeAll
    static void startContainer() throws IOException {
        SmsClientResponse response = new SmsClientResponse();
        SmsClientResult result = new SmsClientResult();
        SmsClientStatus status = new SmsClientStatus();
        status.setCode(0);
        status.setDescription("ok");
        result.setMsgId("087fsdd1aam4vi8t");
        result.setStatus(status);
        response.setResult(result);

        mockServer
                .start();
        new MockServerClient(mockServer.getHost(), mockServer.getServerPort())
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/sms/v1/sms")
                )
                .respond(
                        response()
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(new ObjectMapper().writeValueAsString(response)));
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("sms.api.url", () -> "http://" + mockServer.getHost() + ":" + mockServer.getServerPort());
    }

    @BeforeEach
    public void setup() {
        Recipient recipient = new Recipient();
        recipient.setId(recipientId);
        recipient.setFullName("Aksenov Aleksandr");
        recipient.setType(RecipientType.INDIVIDUAL_PERSON);
        recipient.setEmail("test-alta-cloud@yandex.ru");
        recipient.setPhone("79856213510");
//        recipient.setEmail("n.pletnev@alta-group.ru");
//        recipient.setPhone("79629132587");
        Recipient savedRecipient = recipientRepository.save(recipient);

        Station station = new Station();
        station.setId(stationId);
        station.setRecipient(savedRecipient);
        station.setName("NAME");
        stationRepository.save(station);

        Dnd dnd = new Dnd();
        dnd.setIsActive(false);
        dnd.setRecipient(savedRecipient);
        dndRepository.save(dnd);

        NoticeSetting noticeSetting = new NoticeSetting(Notice.SYSTEM);
        noticeSetting.setByEmail(true);
        noticeSetting.setBySms(true);
        noticeSetting.setFrequency(NoticeFrequency.HOURLY);
        noticeSetting.setRecipient(savedRecipient);
        noticeSettingRepository.save(noticeSetting);

        NoticeSetting noticeSetting1 = new NoticeSetting(Notice.SLUDGE_LEVEL);
        noticeSetting1.setByEmail(true);
        noticeSetting1.setBySms(true);
        noticeSetting1.setFrequency(NoticeFrequency.HOURLY);
        noticeSetting1.setRecipient(savedRecipient);
        noticeSettingRepository.save(noticeSetting1);

        savedRecipient.setDnd(dnd);
        savedRecipient.getStations().add(station);
        savedRecipient.getNoticeSettings().add(noticeSetting);
        savedRecipient.getNoticeSettings().add(noticeSetting1);
        recipientRepository.save(savedRecipient);
    }

    @AfterEach
    public void tearDown() {
        stationRepository.deleteById(stationId);
        dndRepository.deleteAll();
        noticeSettingRepository.deleteAll();
        notificationRepository.deleteAll();
        recipientRepository.deleteById(recipientId);
    }

    @Test
    public void testFrequencyFilterShouldSendOnlyOneEachMessage() throws MessagingException {
        StationNotification event = new StationNotification();
        event.setId(UUID.randomUUID());
        event.setTimestamp(DateTime.now().getMillis());
        event.setStationId(stationId);
        StationNoticeMessage message = new StationNoticeMessage();
        message.setType(StationNotificationMessageType.STATION_EVENT);
        message.setEvent(StationNotificationEventType.SLUDGE_RESET);
        message.setCode("123");
        event.setMessage(message);

        notificationEventHandler.handle(event);

        StationNotification event2 = new StationNotification();
        event2.setId(UUID.randomUUID());
        event2.setTimestamp(DateTime.now().getMillis());
        event2.setStationId(stationId);
        StationNoticeMessage message1 = new StationNoticeMessage();
        message1.setType(StationNotificationMessageType.STATION_EVENT);
        message1.setEvent(StationNotificationEventType.SLUDGE_RESET);
        message1.setCode("123");
        event2.setMessage(message1);
        notificationEventHandler.handle(event2);

        Assertions.assertEquals(2, notificationRepository.count());
    }
}
