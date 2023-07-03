package ru.altagroup.notificationcenter.entities;

import lombok.Data;

@Data
public class ErrorCode {
    private String code;
    private String description;
    private String fullDescription;
    private String level;
    private String recommendations;
    private Notice service;
}
