package ru.altagroup.notificationcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SmsTemplate {
    private final String from = "ALTA-GROUP";
    private long to;
    private String message;
}
