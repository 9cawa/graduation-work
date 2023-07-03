package ru.altagroup.notificationcenter.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ru.altagroup.notificationcenter.dto.EmailMessage;
import ru.altagroup.notificationcenter.entities.MessageType;

import java.util.HashMap;

@Component
public class DealerUnsubscriptionMessageFactory extends MimeMessageCreator implements MessageFactory {

    private final SpringTemplateEngine templateEngine;
    @Value("${spring.mail.username}") String mailFrom;
    private final String UNSUBSCRIPTION_MESSAGE = "Ваша станция %s была отписана от сервисного центра \"%s\"";

    public DealerUnsubscriptionMessageFactory(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        super(mailSender);
        this.templateEngine = templateEngine;
    }

    @Override
    public String createSmsText(String... params) {
        String stationName = params[0];
        String serviceName = params[1];
        return String.format(UNSUBSCRIPTION_MESSAGE, stationName, serviceName);
    }

    @Override
    public String createHtmlEmailText(long timestamp, String... params) {
        String stationName = params[0];
        String serviceName = params[1];
        EmailMessage emailMessage = EmailMessage.builder()
                .timestamp(formatTime(timestamp))
                .description(String.format(UNSUBSCRIPTION_MESSAGE, stationName, serviceName))
                .stationName(stationName)
                .type(MessageType.UNSUBSCRIPTION_NOTIFICATION_USER)
                .build();
        Context context = new Context();
        context.setVariables(new HashMap<>(){{put("message", emailMessage);}});
        return templateEngine.process(NOTIFICATION_TEMPLATE, context);
    }

}
