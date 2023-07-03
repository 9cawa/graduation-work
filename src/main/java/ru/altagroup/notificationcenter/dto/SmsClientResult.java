package ru.altagroup.notificationcenter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SmsClientResult {

    private SmsClientStatus status;
    @JsonProperty("msg_id")
    private String msgId;
}
