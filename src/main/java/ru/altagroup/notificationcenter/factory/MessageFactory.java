package ru.altagroup.notificationcenter.factory;

import ru.altagroup.notificationcenter.dto.SmsTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public interface MessageFactory {

    String NOTIFICATION_TEMPLATE = "notification.html";
    String ERROR_TEMPLATE = "index.html";

    String createSmsText(String... params);

    String createHtmlEmailText(long timestamp, String... params);

    MimeMessage createEmail(String email, String text) throws MessagingException;

    default SmsTemplate createSms(String phone, String text) {
        return new SmsTemplate(Long.parseLong(phone), text);
    }

    default String formatTime(long timestamp) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
        return formatter.format(time);
    }
}
