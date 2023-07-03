package ru.altagroup.notificationcenter.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import ru.altagroup.notificationcenter.dto.EmailMessage;
import ru.altagroup.notificationcenter.entities.MessageType;

import java.util.HashMap;

@Component
public class HealthResetMessageFactory extends MimeMessageCreator implements MessageFactory {

    private final SpringTemplateEngine templateEngine;
    @Value("${spring.mail.username}") String mailFrom;
    private final String HEALTH_RESET_MESSAGE = "Состояние станции %s успешно обновлено";

    public HealthResetMessageFactory(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        super(mailSender);
        this.templateEngine = templateEngine;
    }

    @Override
    public String createSmsText(String... params) {
        String stationName = params[0];
        return String.format(HEALTH_RESET_MESSAGE, stationName);
    }

    @Override
    public String createHtmlEmailText(long timestamp, String... params) {
        String stationName = params[0];
        EmailMessage emailMessage = EmailMessage.builder()
                .timestamp(formatTime(timestamp))
                .description(String.format(HEALTH_RESET_MESSAGE, stationName))
                .stationName(stationName)
                .type(MessageType.STATION_EVENT)
                .build();
        Context context = new Context();
        context.setVariables(new HashMap<>(){{put("message", emailMessage);}});
        return templateEngine.process(NOTIFICATION_TEMPLATE, context);
    }
}
