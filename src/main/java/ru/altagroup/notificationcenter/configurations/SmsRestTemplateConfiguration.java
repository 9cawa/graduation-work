package ru.altagroup.notificationcenter.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import ru.altagroup.notificationcenter.exceptions.ClientApiException;

import java.io.IOException;
import java.time.Duration;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

@Configuration
@Slf4j
public class SmsRestTemplateConfiguration {

    @Value("${sms.api.url}")
    private String rootUri;
    @Value("${sms.api.auth}")
    private String basicAuth;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(30))
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .rootUri(rootUri)
                .errorHandler(responseErrorHandler())
                .build();
    }

    @Bean
    public ResponseErrorHandler responseErrorHandler() {
        return new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return response.getStatusCode().series().equals(SERVER_ERROR) || response.getStatusCode().series().equals(CLIENT_ERROR);
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getStatusCode().is4xxClientError()) {
                    log.error("An client error was encountered during a request to the sms api server. Error code: {}. Response body: {}",
                            response.getStatusCode(), response.getStatusText());
                    throw new IllegalArgumentException("Bad request. Response status: " + response.getStatusText());
                }

                if (response.getStatusCode().is5xxServerError()) {
                    log.error("An server error was encountered during a request to the sms api server. Error code: {}. Response body: {}",
                            response.getStatusCode(), response.getStatusText());
                    throw new ClientApiException("Sms server error. Response status: " + response.getStatusText());
                }
            }
        };
    }
}
