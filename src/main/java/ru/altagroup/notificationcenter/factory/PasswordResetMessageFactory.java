package ru.altagroup.notificationcenter.factory;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import ru.altagroup.notificationcenter.dto.EmailMessage;
import ru.altagroup.notificationcenter.entities.MessageType;

import java.util.HashMap;

@Component
public class PasswordResetMessageFactory extends MimeMessageCreator implements MessageFactory {

    private final SpringTemplateEngine templateEngine;
    @Value("${spring.mail.username}") String mailFrom;
    private final String PASSWORD_RESET_MESSAGE = "Ваш пароль для сервиса Altabio был изменён. Если это были не вы, обратитесь в службу техподдержки.";

    public PasswordResetMessageFactory(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        super(mailSender);
        this.templateEngine = templateEngine;
    }

    @Override
    public String createSmsText(String... params) {
        throw new NotImplementedException("Email message for VERIFICATION_CODE not implemented");
    }

    @Override
    public String createHtmlEmailText(long timestamp, String... params) {
        EmailMessage emailMessage = EmailMessage.builder()
                .timestamp(formatTime(timestamp))
                .type(MessageType.USER_EVENT)
                .description(PASSWORD_RESET_MESSAGE)
                .build();
        Context context = new Context();
        context.setVariables(new HashMap<>(){{put("message", emailMessage);}});
        return templateEngine.process(NOTIFICATION_TEMPLATE, context);
    }
}
