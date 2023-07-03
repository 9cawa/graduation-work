package ru.altagroup.notificationcenter.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
public class StationBindingEvent {
    @NotBlank
    private String name;
    @NotBlank
    @JsonProperty("station_id")
    private UUID stationId;
    @NotBlank
    @JsonProperty("owner_id")
    private UUID ownerId;
    @NotBlank
    private String event;
}
