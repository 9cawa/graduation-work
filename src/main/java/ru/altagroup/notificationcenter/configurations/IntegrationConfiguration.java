package ru.altagroup.notificationcenter.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.mail.MailSendingMessageHandler;
import org.springframework.integration.transformer.Transformer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.client.RestTemplate;
import ru.altagroup.notificationcenter.configurations.filters.MainEmailFilter;
import ru.altagroup.notificationcenter.configurations.filters.MainSmsFilter;
import ru.altagroup.notificationcenter.dto.MessageDto;
import ru.altagroup.notificationcenter.dto.SmsClientResponse;
import ru.altagroup.notificationcenter.dto.SmsTemplate;
import ru.altagroup.notificationcenter.entities.*;
import ru.altagroup.notificationcenter.factory.MessageFactory;
import ru.altagroup.notificationcenter.factory.MessageFactoryBuilder;
import ru.altagroup.notificationcenter.repositories.NotificationRepository;

import javax.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class IntegrationConfiguration {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;
    private final NotificationRepository notificationRepository;
    private final MessageFactoryBuilder messageFactoryBuilder;
    private final MainEmailFilter mainEmailFilter;
    private final MainSmsFilter mainSmsFilter;

    @Bean
    public MessageChannel sendEmailChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel sendSmsChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel smsResponseChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow emailFlow() {
        return IntegrationFlows.from("sendEmailChannel")
                .filter(mainEmailFilter, "accept")
                .intercept(saveNotificationBeforeSendEmail())
                .transform(mimeMessageTransformer())
                .handle(new MailSendingMessageHandler(mailSender))
                .get();
    }

    private ChannelInterceptor saveNotificationBeforeSendEmail() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> msg, MessageChannel channel) {
                MessageDto message = (MessageDto) msg.getPayload();
                MessageHeaders headers = msg.getHeaders();
                Recipient recipient = headers.get("recipient", Recipient.class);
                Notice notice = headers.get("notice", Notice.class);
                Notification notification = new Notification();
                notification.setChannel(Channel.EMAIL);
                notification.setTimestamp(Instant.now().toEpochMilli());
                notification.setDelivered(Boolean.TRUE);
                notification.setText(getFromHtml(message));
                notification.setEvent(message.getEventType());
                notification.setNotice(notice);
                notification.setRecipient(recipient);
                notificationRepository.save(notification);
                return ChannelInterceptor.super.preSend(msg, channel);
            }
        };
    }

    private String getFromHtml(MessageDto message) {
        String html = message.getText();
        Document doc = Jsoup.parse(html);
        Element link = doc.select(".paragraph").first();
        if (link == null) return message.getText();
        return link.text();
    }

    private Transformer mimeMessageTransformer() {
        return msg -> {
            MessageDto message = (MessageDto) msg.getPayload();
            MessageHeaders headers = msg.getHeaders();
            Recipient recipient = headers.get("recipient", Recipient.class);
            MessageFactory messageFactory = messageFactoryBuilder.getMessageFactory(message.getEventType());
            try {
                assert recipient != null;
                MimeMessage mimeMessage = messageFactory.createEmail(recipient.getEmail(), message.getText());
                return new GenericMessage<>(mimeMessage, headers);
            } catch (javax.mail.MessagingException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    public IntegrationFlow smsFlow() {
        return IntegrationFlows.from("sendSmsChannel")
                .enrichHeaders(enricherSpec -> {
                    enricherSpec
                            .headerExpression("type", "payload.eventType")
                            .headerExpression("message", "payload.text")
                            .headerExpression("event_id", "payload.id");
                })
                .filter(mainSmsFilter, "accept")
                .transform(smsTransformer())
                .handle(Http.outboundGateway("/sms/v1/sms", restTemplate)
                        .httpMethod(HttpMethod.POST)
                        .extractPayload(true)
                        .expectedResponseType(SmsClientResponse.class)
                )
                .handle(handleSmsResponse())
                .get();
    }

    private Transformer smsTransformer() {
        return msg -> {
            MessageDto message = (MessageDto) msg.getPayload();
            MessageHeaders headers = msg.getHeaders();
            Recipient recipient = headers.get("recipient", Recipient.class);
            MessageFactory messageFactory = messageFactoryBuilder.getMessageFactory(message.getEventType());
            assert recipient != null;
            SmsTemplate sms = messageFactory.createSms(recipient.getPhone(), message.getText());
            return new GenericMessage<>(sms, headers);
        };
    }

    private MessageHandler handleSmsResponse() {
        return message -> {
            SmsClientResponse response = (SmsClientResponse) message.getPayload();
            MessageHeaders headers = message.getHeaders();
            Recipient recipient = headers.get("recipient", Recipient.class);
            Notice notice = headers.get("notice", Notice.class);
            EventType type = headers.get("type", EventType.class);
            String text = headers.get("message", String.class);
            UUID id = headers.get("event_id", UUID.class);
            if (response.getResult().getStatus().getCode() != 0) {
                log.error(String.format("%s msg_id: %s",
                        response.getResult().getStatus().getDescription(), response.getResult().getMsgId()));
                return;
            }
            Notification notification = new Notification();
            notification.setChannel(Channel.SMS);
            notification.setTimestamp(Instant.now().toEpochMilli());
            notification.setDelivered(Boolean.TRUE);
            notification.setText(text);
            notification.setEvent(type);
            notification.setNotice(notice);
            notification.setRecipient(recipient);
            notificationRepository.save(notification);
        };
    }

}
