package ru.altagroup.notificationcenter.components.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.joda.time.DateTime;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import ru.altagroup.notificationcenter.events.UserNotificationEvent;
import ru.altagroup.notificationcenter.handlers.NotificationEventHandler;
import ru.altagroup.notificationcenter.repositories.*;
import ru.altagroup.notificationcenter.services.MessageGateway;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
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
public class NotificationEventHandlerTest {
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

    @RegisterExtension
    static WireMockExtension errorsService = WireMockExtension.newInstance().options(wireMockConfig().port(8090)).build();
    private final String GET_ERROR = "/errors/E200011";


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
//        recipient.setEmail("a.aksenov@alta-group.ru");
//        recipient.setEmail("n.pletnev@alta-group.ru");
//        recipient.setPhone("79629132587");
        Recipient savedRecipient = recipientRepository.save(recipient);

        Station station = new Station();
        station.setId(stationId);
        station.setRecipient(savedRecipient);
        station.setName("NAME");
        stationRepository.save(station);

        Dnd dnd = new Dnd();
        dnd.setActive(false);
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
    public void testSendErrorWhenErrorCodeIsNullThenThrowAssertionsError() {
        StationNotificationEvent event = new StationNotificationEvent();
        event.setId(UUID.randomUUID());
        event.setTimestamp(DateTime.now().getMillis());
        event.setStationId(stationId);
        StationNotificationEvent.Message message =
                new StationNotificationEvent.Message(MessageType.ERROR_EVENT, null, EventType.ERROR);
        event.setMessage(message);

        Assertions.assertThrows(AssertionError.class, () -> notificationEventHandler.handle(event));
    }

    @Test
    public void testSendError() throws MessagingException {
        errorsService.stubFor(get(urlPathEqualTo(GET_ERROR))
                .willReturn(okForJson(errorCode()).withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)));

        StationNotificationEvent event = new StationNotificationEvent();
        event.setId(UUID.randomUUID());
        event.setTimestamp(DateTime.now().getMillis());
        event.setStationId(stationId);
        StationNotificationEvent.Message message =
                new StationNotificationEvent.Message(MessageType.ERROR_EVENT, "E200011", EventType.ERROR);
        event.setMessage(message);
        notificationEventHandler.handle(event);

        Assertions.assertEquals(2, notificationRepository.count());
    }

    @Test
    public void testSendSludgeReset() throws MessagingException {
        StationNotificationEvent event = new StationNotificationEvent();
        event.setId(UUID.randomUUID());
        event.setTimestamp(DateTime.now().getMillis());
        event.setStationId(stationId);
        StationNotificationEvent.Message message =
                new StationNotificationEvent.Message(MessageType.STATION_EVENT, null, EventType.SLUDGE_RESET);
        event.setMessage(message);

        notificationEventHandler.handle(event);
        Assertions.assertEquals(2, notificationRepository.count());
    }

    @Test
    public void testSendHealthReset() throws MessagingException {
        StationNotificationEvent event = new StationNotificationEvent();
        event.setId(UUID.randomUUID());
        event.setTimestamp(DateTime.now().getMillis());
        event.setStationId(stationId);
        StationNotificationEvent.Message message =
                new StationNotificationEvent.Message(MessageType.STATION_EVENT, null, EventType.HEALTH_RESET);
        event.setMessage(message);

        notificationEventHandler.handle(event);
        Assertions.assertEquals(2, notificationRepository.count());
    }

    @Test
    public void testSendPasswordReset() throws MessagingException {
        UserNotificationEvent event = new UserNotificationEvent();
        event.setId(UUID.randomUUID());
        event.setTimestamp(DateTime.now().getMillis());
        event.setUserId(recipientId);
        event.setEmail("test-alta-cloud@yandex.ru");
        event.setPhone("79856213510");
        UserNotificationEvent.Message message =
                new UserNotificationEvent.Message(MessageType.USER_EVENT, EventType.PASSWORD_RESET, null);
        event.setMessage(message);

        notificationEventHandler.handle(event);
        Assertions.assertEquals(1, notificationRepository.count());
    }

    @Test
    public void testSendVerificationCode() throws MessagingException {
        UserNotificationEvent event = new UserNotificationEvent();
        event.setId(UUID.randomUUID());
        event.setTimestamp(DateTime.now().getMillis());
        event.setUserId(recipientId);
        event.setEmail("test-alta-cloud@yandex.ru");
        event.setPhone("79856213510");
//        event.setEmail("a.aksenov@alta-group.ru");
//        event.setPhone("79629132587");
        UserNotificationEvent.Message message =
                new UserNotificationEvent.Message(MessageType.USER_EVENT, EventType.VERIFICATION_CODE, "123456");
        event.setMessage(message);

        notificationEventHandler.handle(event);
        Assertions.assertEquals(1, notificationRepository.count());
    }

    private ErrorCode errorCode() {
        ErrorCode errorCode = new ErrorCode();
        errorCode.setCode("E200011");
        errorCode.setDescription("DOT, короткое замыкание.");
        errorCode.setFullDescription("Короткое замыкание одного из каналов нагрузки модуля коммутации силовой нагрузки (превышение значения тока 20А в течение не менее 40 мс).");
        errorCode.setLevel("Критическая ошибка");
        errorCode.setRecommendations("Отключить питание DOT. Отключить разъемы каналов 1, 2 и 3. С помощью мультиметра измерить сопротивление цепи «фаза-нейтраль» разъема отходящего кабеля питания насосов. Выявленный насос с коротким замыканием вывести из работы.");
        errorCode.setService(Notice.SYSTEM);
        return errorCode;
    }

}
