package ru.altagroup.notificationcenter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;
import java.time.ZoneId;

@Data
@AllArgsConstructor
public class DndRequest {
    @NotNull
    private boolean isActive;
    @NotNull
    private LocalTime begin;
    @NotNull
    private LocalTime end;
    @NotNull
    private ZoneId zoneId;
}
