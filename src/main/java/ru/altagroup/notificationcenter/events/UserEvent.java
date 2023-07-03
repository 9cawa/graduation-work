package ru.altagroup.notificationcenter.events;

import lombok.Data;
import ru.altagroup.notificationcenter.entities.RecipientType;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.UUID;

@Data
public class UserEvent {
    @NotNull
    private UUID id;
    @NotBlank
    private RecipientType type;
    @NotBlank
    private String fullName;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Pattern(regexp = "^7\\d{10}")
    private String phone;
    @NotBlank
    private String event;
}

