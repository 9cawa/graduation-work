package ru.altagroup.notificationcenter.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.altacloud.v2.avro.UserEvent;
import ru.altacloud.v2.avro.UserEventType;
import ru.altacloud.v2.avro.UserType;
import ru.altagroup.notificationcenter.AuthenticationTestService;
import ru.altagroup.notificationcenter.dto.DndRequest;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.exceptions.NotFoundException;
import ru.altagroup.notificationcenter.handlers.RecipientCreateEventHandler;
import ru.altagroup.notificationcenter.handlers.RecipientDeleteEventHandler;
import ru.altagroup.notificationcenter.repositories.DndRepository;
import ru.altagroup.notificationcenter.repositories.NoticeSettingRepository;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

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
public class DndControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private DndRepository dndRepository;
    @Autowired private NoticeSettingRepository noticeSettingRepository;
    @Autowired private RecipientCreateEventHandler createHandler;
    @Autowired private RecipientDeleteEventHandler deleteHandler;
    @Autowired private RecipientRepository recipientRepository;
    private final AuthenticationTestService authenticationTestService = new AuthenticationTestService();

    private final String GET_SET_DND_URI = "/recipients/dnd";
    private final UUID recipientId = UUID.fromString("5c3500e6-b0c8-4f16-bb07-5a21fb5ae8f0");

    @BeforeEach
    public void setup() {
        UserEvent event = new UserEvent();
        event.setId(recipientId);
        event.setType(UserType.INDIVIDUAL_PERSON);
        event.setFullName("Aleksandr Aksenov");
        event.setEmail("a.aksenov@alta-group.ru");
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
        deleteEvent.setEmail("a.aksenov@alta-group.ru");
        deleteEvent.setPhone("79998887766");
        deleteEvent.setEvent(UserEventType.DELETE);
        deleteHandler.handle(deleteEvent);
    }

    @Test
    public void testUserAndSettingsCreatedSuccessfully() {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("not found with id: " + recipientId));

        Assertions.assertEquals(1, dndRepository.count());
        Assertions.assertEquals(4, noticeSettingRepository.count());
        Assertions.assertTrue(recipient.getDnd().getIsActive());
    }

    @Test
    public void testGetDndStatusWhenAllIsOk() throws Exception {
        var auth = authenticationTestService.auth("a.aksenov@alta-group.ru", "12345678");

        mockMvc.perform(get(GET_SET_DND_URI)
                        .header(HttpHeaders.AUTHORIZATION, auth.getToken_type() + " " + auth.getAccess_token())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testGetDndStatusWhenRecipientNotFound() throws Exception {
        var auth = authenticationTestService.auth("n.pletnev@alta-group.ru", "12345678");

        mockMvc.perform(get(GET_SET_DND_URI)
                        .header(HttpHeaders.AUTHORIZATION, auth.getToken_type() + " " + auth.getAccess_token())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testSetDndStatusWhenAllIsOk() throws Exception {
        ObjectMapper mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        var auth = authenticationTestService.auth("a.aksenov@alta-group.ru", "12345678");

        mockMvc.perform(put(GET_SET_DND_URI)
                        .header(HttpHeaders.AUTHORIZATION, auth.getToken_type() + " " + auth.getAccess_token())
                        .content(mapper.writeValueAsString(dndRequest()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(recipientId.toString()))
                .andExpect(jsonPath("$.zoneId").value(ZoneId.ofOffset("GMT", ZoneOffset.of("-06:05:20")).toString()))
                .andDo(print());
    }

    @Test
    public void testSetDndStatusWhenRecipientNotFound() throws Exception {
        ObjectMapper mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        var auth = authenticationTestService.auth("n.pletnev@alta-group.ru", "12345678");

        mockMvc.perform(put(GET_SET_DND_URI)
                        .header(HttpHeaders.AUTHORIZATION, auth.getToken_type() + " " + auth.getAccess_token())
                        .content(mapper.writeValueAsString(dndRequest()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    private DndRequest dndRequest() {
        return new DndRequest(true, LocalTime.of(22,0), LocalTime.of(8, 30),
                ZoneId.ofOffset("GMT", ZoneOffset.of("-06:05:20")));
    }
}
