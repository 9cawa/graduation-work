package ru.altagroup.notificationcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsClientMessage {
   private String to;
   private String message;
}
