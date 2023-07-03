package ru.altagroup.notificationcenter.services;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import ru.altagroup.notificationcenter.dto.MessageDto;
import ru.altagroup.notificationcenter.entities.Notice;
import ru.altagroup.notificationcenter.entities.Recipient;

@MessagingGateway
public interface MessageGateway {

    @Gateway(requestChannel = "sendSmsChannel")
    void sendSms(@Payload MessageDto message, @Header("recipient") Recipient recipient, @Header("notice") Notice notice);

    @Gateway(requestChannel = "sendEmailChannel")
    void sendEmail(@Payload MessageDto message, @Header("recipient") Recipient recipient, @Header("notice") Notice notice);
}
