package ru.altagroup.notificationcenter.integration;//package ru.altagroup.notificationcenter.integration;
//
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import ru.altagroup.notificationcenter.entities.EventType;
//import ru.altagroup.notificationcenter.dto.SmsTemplate;
//import ru.altagroup.notificationcenter.services.MessageGateway;
//
//import java.util.UUID;
//
//@SpringBootTest
//@Disabled
//public class SmsIntegrationTest {
//
//    @Autowired private MessageGateway gateway;
//
//    @Test
//    public void testSendSms() throws InterruptedException {
//        SmsTemplate message = new SmsTemplate(79629132587L, "Тестовое сообщение!");
//        gateway.sendSms(message, EventType.VERIFICATION_CODE.name(), UUID.randomUUID());
//        Thread.sleep(5000);
//    }
//}
