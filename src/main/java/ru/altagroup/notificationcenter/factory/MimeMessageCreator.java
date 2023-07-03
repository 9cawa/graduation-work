package ru.altagroup.notificationcenter.factory;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public abstract class MimeMessageCreator {

    @Value("${spring.mail.username}") String mailFrom;
    private final JavaMailSender mailSender;

    public MimeMessage createEmail(String email, String text) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        helper.setFrom(String.format("Alta Cloud<%s>", mailFrom));
        helper.setTo(email);
        helper.setSubject("ALTA ONLINE");
        helper.setText(text, true);
        return mimeMessage;
    }
}
