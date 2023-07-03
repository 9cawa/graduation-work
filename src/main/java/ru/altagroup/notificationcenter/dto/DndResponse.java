package ru.altagroup.notificationcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DndResponse {

    private UUID userId;
    private Boolean isActive;
    private LocalTime begin;
    private LocalTime end;
    private String zoneId;
}
