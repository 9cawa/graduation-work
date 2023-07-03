package ru.altagroup.notificationcenter.factory;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component
@RequiredArgsConstructor
public class VerificationCodeMessageFactory implements MessageFactory {

    private final String VERIFICATION_CODE_MESSAGE = "Ваш код подтверждения: %s Если это были не вы, проигнорируйте данное сообщение.";

    @Override
    public String createSmsText(String... params) {
        String code = params[0];
        return String.format(VERIFICATION_CODE_MESSAGE, code);
    }

    @Override
    public String createHtmlEmailText(long timestamp, String... params) {
        throw new NotImplementedException("Email message for VERIFICATION_CODE not implemented");
    }

    @Override
    public MimeMessage createEmail(String email, String text) {
        throw new NotImplementedException("Email message for VERIFICATION_CODE not implemented");
    }
}
