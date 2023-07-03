package ru.altagroup.notificationcenter.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.altacloud.v2.avro.UserEvent;
import ru.altacloud.v2.avro.UserEventType;
import ru.altacloud.v2.avro.UserType;
import ru.altagroup.notificationcenter.AuthenticationTestService;
import ru.altagroup.notificationcenter.dto.UpdateServiceNotificationDto;
import ru.altagroup.notificationcenter.handlers.RecipientEventHandler;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:postgresql:14.5-alpine:///test?TC_DAEMON=true"
})
public class NotificationControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Qualifier("recipientCreateEventHandler")
    @Autowired private RecipientEventHandler createHandler;
    @Qualifier("recipientDeleteEventHandler")
    @Autowired private RecipientEventHandler deleteHandler;
    private final AuthenticationTestService authenticationTestService = new AuthenticationTestService();

    private final String GET_NOTIFICATION_SETTINGS = "/recipients/notices";
    private final String UPDATE_NOTIFICATION_SETTINGS = "/recipients/notice";
    private final UUID recipientId = UUID.fromString("5c3500e6-b0c8-4f16-bb07-5a21fb5ae8f0");

    @BeforeEach
    public void setup() {
        UserEvent event = new UserEvent();
        event.setId(recipientId);
        event.setType(UserType.INDIVIDUAL_PERSON);
        event.setFullName("Aleksandr Aksenov");
        event.setEmail("test@test.ru");
        event.setPhone("79998887766");
        event.setEvent(UserEventType.CREATE);
        createHandler.handle(event);
    }

    @AfterEach
    public void tearDown() {
        UserEvent deleteEvent = new UserEvent();
        deleteEvent.setId(recipientId);
        deleteEvent.setType(UserType.INDIVIDUAL_PERSON);
        deleteEvent.setFullName("Aleksandr Aksenov");
        deleteEvent.setEmail("test@test.ru");
        deleteEvent.setPhone("79998887766");
        deleteEvent.setEvent(UserEventType.DELETE);
        deleteHandler.handle(deleteEvent);
    }

    @Test
    public void testGetNotificationSettingsWhenAllIsOk() throws Exception {
        var auth = authenticationTestService.auth("test@test.ru", "12345678");

        mockMvc.perform(get(GET_NOTIFICATION_SETTINGS)
                        .header(HttpHeaders.AUTHORIZATION, auth.getToken_type() + " " + auth.getAccess_token())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andDo(print());
    }

    @Test
    public void testGetNotificationSettingsWhenRecipientNotFound() throws Exception {
        var auth = authenticationTestService.auth("n.pletnev@alta-group.ru", "12345678");

        mockMvc.perform(get(GET_NOTIFICATION_SETTINGS)
                        .header(HttpHeaders.AUTHORIZATION, auth.getToken_type() + " " + auth.getAccess_token())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Пользователь не найден с идентификатором:")))
                .andDo(print());
    }

    @Test
    public void testUpdateNotificationSettingsWhenAllIsOk() throws Exception {
        var auth = authenticationTestService.auth("test@test.ru", "12345678");

        mockMvc.perform(put(UPDATE_NOTIFICATION_SETTINGS)
                        .header(HttpHeaders.AUTHORIZATION, auth.getToken_type() + " " + auth.getAccess_token())
                        .content(new ObjectMapper().writeValueAsString(updateServiceNotificationDto()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifyByEmail").value("false"))
                .andDo(print());
    }

    @Test
    public void testUpdateNotificationSettingsWhenRecipientNotFound() throws Exception {
        var auth = authenticationTestService.auth("n.pletnev@alta-group.ru", "12345678");

        mockMvc.perform(put(UPDATE_NOTIFICATION_SETTINGS)
                        .header(HttpHeaders.AUTHORIZATION, auth.getToken_type() + " " + auth.getAccess_token())
                        .content(new ObjectMapper().writeValueAsString(updateServiceNotificationDto()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Пользователь не найден с идентификатором:")))
                .andDo(print());
    }

    private UpdateServiceNotificationDto updateServiceNotificationDto() {
        return UpdateServiceNotificationDto
                .builder()
                .name("TEMPERATURE")
                .notifyByEmail(false)
                .notifyBySms(true)
                .frequency("MONTHLY")
                .build();
    }
}
