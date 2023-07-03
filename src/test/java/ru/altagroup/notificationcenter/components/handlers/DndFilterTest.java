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
import ru.altagroup.notificationcenter.dto.SmsClientResponse;
import ru.altagroup.notificationcenter.dto.SmsClientResult;
import ru.altagroup.notificationcenter.dto.SmsClientStatus;
import ru.altagroup.notificationcenter.entities.*;
import ru.altagroup.notificationcenter.events.StationNotificationEvent;
import ru.altagroup.notificationcenter.handlers.NotificationEventHandler;
import ru.altagroup.notificationcenter.repositories.*;
import ru.altagroup.notificationcenter.services.MessageGateway;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalTime;
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
public class DndFilterTest {

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
        Recipient savedRecipient = recipientRepository.save(recipient);

        Station station = new Station();
        station.setId(stationId);
        station.setRecipient(savedRecipient);
        station.setName("NAME");
        stationRepository.save(station);

        Dnd dnd = new Dnd();
        dnd.setActive(Boolean.TRUE);
        dnd.setZoneId("UTC");
//        dnd.setStartTime(LocalTime.of(20,0));
//        dnd.setEndTime(LocalTime.of(8,30));
        dnd.setStartTime(LocalTime.now().minusHours(1));
        dnd.setEndTime(LocalTime.now().plusHours(1));
        dnd.setRecipient(savedRecipient);
        dndRepository.save(dnd);

        NoticeSetting noticeSetting = new NoticeSetting(Notice.SLUDGE_LEVEL);
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

    @AfterEach
    public void tearDown() {
        stationRepository.deleteById(stationId);
        dndRepository.deleteAll();
        noticeSettingRepository.deleteAll();
        notificationRepository.deleteAll();
        recipientRepository.deleteById(recipientId);
    }

    @Test
    public void testDndFilterShouldNotSendMessages() throws MessagingException {
        StationNotificationEvent event = new StationNotificationEvent();
        event.setId(UUID.randomUUID());
        event.setTimestamp(DateTime.now().getMillis());
        event.setStationId(stationId);
        StationNotificationEvent.Message message =
                new StationNotificationEvent.Message(MessageType.STATION_EVENT, null, EventType.SLUDGE_RESET);
        event.setMessage(message);

        notificationEventHandler.handle(event);
        Assertions.assertEquals(0, notificationRepository.count());
    }
}
