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
public class EquipmentReplacementFactory extends MimeMessageCreator implements MessageFactory {

    private final SpringTemplateEngine templateEngine;
    @Value("${spring.mail.username}") String mailFrom;
    private final String EQUIPMENT_REPLACE_MESSAGE = "На станции %s была произведена замена оборудования: %s. " +
            "Дата введения в эксплуатацию: %s";

    public EquipmentReplacementFactory(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        super(mailSender);
        this.templateEngine = templateEngine;
    }

    @Override
    public String createSmsText(String... params) {
        String stationName = params[0];
        String deviceName = params[1];
        String commissioning = params[2];
        return String.format(EQUIPMENT_REPLACE_MESSAGE, stationName, deviceName, commissioning);
    }

    @Override
    public String createHtmlEmailText(long timestamp, String... params) {
        String stationName = params[0];
        String deviceName = params[1];
        String commissioning = params[2];
        EmailMessage emailMessage = EmailMessage.builder()
                .timestamp(formatTime(timestamp))
                .description(String.format(EQUIPMENT_REPLACE_MESSAGE, stationName, deviceName, commissioning))
                .stationName(stationName)
                .type(MessageType.STATION_EVENT)
                .build();
        Context context = new Context();
        context.setVariables(new HashMap<>(){{put("message", emailMessage);}});
        return templateEngine.process(NOTIFICATION_TEMPLATE, context);
    }

}
