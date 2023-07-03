package ru.altagroup.notificationcenter.exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Builder
@RequiredArgsConstructor
@Getter
public class ExceptionMessage {
    private final LocalDateTime timestamp;
    private final Integer code;
    private final String message;
    private final String uri;
}

