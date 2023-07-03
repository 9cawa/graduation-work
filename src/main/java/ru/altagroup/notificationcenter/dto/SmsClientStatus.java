package ru.altagroup.notificationcenter.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SmsClientStatus {
    private int code;
    private String description;
    private Map<String, String> payload;
}
