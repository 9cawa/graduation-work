package ru.altagroup.notificationcenter.dto;

import lombok.Data;

@Data
public class SmsResponse {

    private String messageId;
    private int code;
    private String description;
}