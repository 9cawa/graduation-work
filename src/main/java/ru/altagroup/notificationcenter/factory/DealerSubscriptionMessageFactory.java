package ru.altagroup.notificationcenter.factory;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import ru.altagroup.notificationcenter.dto.EmailMessage;
import ru.altagroup.notificationcenter.entities.MessageType;

import java.util.HashMap;

@Component
public class DealerSubscriptionMessageFactory extends MimeMessageCreator implements MessageFactory {

    private final SpringTemplateEngine templateEngine;

    public DealerSubscriptionMessageFactory(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        super(mailSender);
        this.templateEngine = templateEngine;
    }

    @Override
    public String createSmsText(String... params) {
        String stationName = params[0];
        String serviceName = params[1];
        String message = "Ваша станция %s успешно привязана к сервисному центру \"%s\"";
        return String.format(message, stationName, serviceName);
    }

    @Override
    public String createHtmlEmailText(long timestamp, String... params) {
        String stationName = params[0];
        String serviceName = params[1];
        String servicePhone = params[2];
        String serviceEmail = params[3];
        String message = "Ваша станция \"%s\" успешно привязана к сервисному центру \"%s\". Информация о сервисном центре: Телефон: %s, Email: %s";
        EmailMessage emailMessage = EmailMessage.builder()
                .timestamp(formatTime(timestamp))
                .description(String.format(message, stationName, serviceName, servicePhone, serviceEmail))
                .stationName(stationName)
                .type(MessageType.SUBSCRIPTION_NOTIFICATION_USER)
                .build();
        Context context = new Context();
        context.setVariables(new HashMap<>(){{put("message", emailMessage);}});
        return templateEngine.process(NOTIFICATION_TEMPLATE, context);
    }

}
