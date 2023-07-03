package ru.altagroup.notificationcenter.controllers;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientException;
import ru.altagroup.notificationcenter.exceptions.ClientApiException;
import ru.altagroup.notificationcenter.exceptions.ExceptionMessage;
import ru.altagroup.notificationcenter.exceptions.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@ControllerAdvice(basePackages = {"ru.altagroup.notificationcenter.controllers"})
public class ControllerExceptionHandler {

    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ExceptionMessage> handleRestClientException(Exception exception, HttpServletRequest request) {
        String message = exception.getMessage();
        ExceptionMessage exceptionMessage = ExceptionMessage.builder()
                .message(message)
                .code(HttpStatus.SERVICE_UNAVAILABLE.value())
                .timestamp(LocalDateTime.now())
                .uri(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(MediaType.APPLICATION_JSON).body(exceptionMessage);
    }

    @ExceptionHandler(ClientApiException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionMessage> handleClientApiException(Exception exception, HttpServletRequest request) {
        String message = exception.getMessage();
        ExceptionMessage exceptionMessage = ExceptionMessage.builder()
                .message(message)
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .uri(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(exceptionMessage);
    }

    @ExceptionHandler(NotImplementedException.class)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ResponseEntity<ExceptionMessage> handleNotImplementedException(Exception exception, HttpServletRequest request) {
        String message = exception.getMessage();
        ExceptionMessage exceptionMessage = ExceptionMessage.builder()
                .message(message)
                .code(HttpStatus.NOT_IMPLEMENTED.value())
                .timestamp(LocalDateTime.now())
                .uri(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).contentType(MediaType.APPLICATION_JSON).body(exceptionMessage);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionMessage> handleBadRequest(Exception exception, HttpServletRequest request) {
        String message = exception.getMessage();
        ExceptionMessage exceptionMessage = ExceptionMessage.builder()
                .message(message)
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .uri(request.getRequestURI())
                .build();
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(exceptionMessage);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ExceptionMessage> handle(Exception exception, HttpServletRequest request) {
        String message = exception.getMessage();
        ExceptionMessage exceptionMessage = ExceptionMessage.builder()
                .message(message)
                .code(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .uri(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(exceptionMessage);
    }


}
