package ru.altagroup.notificationcenter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsClientResponse {
    private SmsClientResult result;
}
