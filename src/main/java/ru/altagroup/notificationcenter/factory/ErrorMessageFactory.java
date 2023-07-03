package ru.altagroup.notificationcenter.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.altagroup.notificationcenter.dto.EmailMessage;
import ru.altagroup.notificationcenter.entities.ErrorCode;
import ru.altagroup.notificationcenter.entities.MessageType;
import ru.altagroup.notificationcenter.exceptions.ExceptionMessage;
import ru.altagroup.notificationcenter.exceptions.NotFoundException;
import ru.altagroup.notificationcenter.exceptions.TimeOutException;

import java.time.Duration;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@Slf4j
public class ErrorMessageFactory extends MimeMessageCreator implements MessageFactory {

    private final SpringTemplateEngine templateEngine;
    private final WebClient webClient;
    @Value("${spring.mail.username}") String mailFrom;
    private final String ERROR_MESSAGE = "Внимание! Ошибка оборудования станции %s. (код ошибки: %s)";
    @Value("${errors-service.url}")
    private String GET_ERROR_URI;

    public ErrorMessageFactory(JavaMailSender mailSender, SpringTemplateEngine templateEngine, WebClient webClient) {
        super(mailSender);
        this.templateEngine = templateEngine;
        this.webClient = webClient;
    }

    @Override
    public String createSmsText(String... params) {
        assert params.length == 2;
        String stationName = params[0];
        String errorCode = params[1];
        return String.format(ERROR_MESSAGE, stationName, errorCode);
    }

    @Override
    public String createHtmlEmailText(long timestamp, String... params) {
        String stationName = params[0];
        String code = params[1];
        ErrorCode errorCode = retrieveErrorCode(code);
        EmailMessage emailMessage = EmailMessage.builder()
                .code(code)
                .timestamp(formatTime(timestamp))
                .description(String.format(ERROR_MESSAGE, stationName, code))
                .fullDescription(errorCode.getFullDescription())
                .recommendations(errorCode.getRecommendations())
                .stationName(stationName)
                .type(MessageType.ERROR_EVENT)
                .build();
        Context context = new Context();
        context.setVariables(getErrorTemplateProperties(emailMessage));
        return templateEngine.process(ERROR_TEMPLATE, context);
    }

    private HashMap<String, Object> getErrorTemplateProperties(EmailMessage message) {
        HashMap<String, Object> hashMap = new HashMap<>();
        HashMap<String, Consumer<EmailMessage>> consumerHashMap = new HashMap<>(){{
            put("E200011", (msg) -> hashMap.put("system", msg));
            put("E200012", (msg) -> hashMap.put("system", msg));
            put("E200013", (msg) -> hashMap.put("system", msg));
            put("E200014", (msg) -> hashMap.put("system", msg));
            put("EA10001", (msg) -> hashMap.put("temperature", msg));
            put("EA10002", (msg) -> hashMap.put("temperature", msg));
            put("EA20001", (msg) -> hashMap.put("pump_resource", msg));
            put("EA20002", (msg) -> hashMap.put("pump_resource", msg));
            put("EA20003", (msg) -> hashMap.put("pump_resource", msg));
            put("EA30001", (msg) -> hashMap.put("sludge_level_text", msg));
            put("EA30002", (msg) -> hashMap.put("sludge_level_text", msg));
        }};
        consumerHashMap.get(message.getCode()).accept(message);

        String formattedString = message.getTimestamp();
        hashMap.put("timestamp", formattedString);
        hashMap.put("stationName", message.getStationName());
        return hashMap;
    }

    public ErrorCode retrieveErrorCode(String code) {
        return webClient.get().uri(uriBuilder ->
                uriBuilder.path(GET_ERROR_URI).build(code))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, handleClientErrorResponse())
                .onStatus(HttpStatus::is5xxServerError, handleServerErrorResponse())
                .bodyToMono(ErrorCode.class)
                .retryWhen(getRetryPolicy())
                .onErrorStop()
                .block(Duration.ofSeconds(1));
    }

    private Retry getRetryPolicy() {
        return Retry.fixedDelay(3, Duration.ofMillis(200))
                .filter(error -> error instanceof TimeOutException || error instanceof WebClientRequestException)
                .doAfterRetry(retrySignal -> log.error("Retry to send request to Service. Reason: {}", retrySignal.failure().getMessage()))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> Exceptions.unwrap(retrySignal.failure()));
    }

    private Function<ClientResponse, Mono<? extends Throwable>> handleServerErrorResponse() {
        return clientResponse -> clientResponse.bodyToMono(ExceptionMessage.class)
                .flatMap(message -> Mono.error(new TimeOutException(clientResponse.statusCode() + " " + message.getMessage())));
    }

    private Function<ClientResponse, Mono<? extends Throwable>> handleClientErrorResponse() {
        return clientResponse -> clientResponse.bodyToMono(ExceptionMessage.class)
                .flatMap(message -> Mono.error(new NotFoundException(clientResponse.statusCode() + " " + message.getMessage())));
    }
}
