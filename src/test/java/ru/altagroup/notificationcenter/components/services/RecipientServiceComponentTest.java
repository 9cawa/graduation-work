package ru.altagroup.notificationcenter.components.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.entities.RecipientType;
import ru.altagroup.notificationcenter.entities.Station;
import ru.altagroup.notificationcenter.events.RecipientEventType;
import ru.altagroup.notificationcenter.events.UserEvent;
import ru.altagroup.notificationcenter.repositories.DndRepository;
import ru.altagroup.notificationcenter.repositories.NoticeSettingRepository;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;
import ru.altagroup.notificationcenter.repositories.StationRepository;
import ru.altagroup.notificationcenter.services.RecipientService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
public class RecipientServiceComponentTest {
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private RecipientService recipientService;
    @Autowired
    private DndRepository dndRepository;
    @Autowired
    private NoticeSettingRepository noticeSettingRepository;
    @Autowired
    private StationRepository stationRepository;

    private final UUID id = UUID.randomUUID();
    private final UUID stationId = UUID.randomUUID();
    private Recipient recipient = new Recipient();
    private Station station = new Station();

    @BeforeEach
    public void setup() {
        UserEvent event = new UserEvent();
        event.setId(id);
        event.setType(RecipientType.INDIVIDUAL_PERSON);
        event.setFullName("Aleksandr Aksenov");
        event.setEmail("test@test.ru");
        event.setPhone("79998887766");
        event.setEvent("CREATE");
        recipientService.listenUserEvent(event);

        recipient = recipientRepository.findById(id).get();
        station.setId(stationId);
        station.setName("name1");
        station.setRecipient(recipient);
        stationRepository.save(station);

        recipient.getStations().add(station);
        recipientRepository.save(recipient);
    }

    @Test
    public void testWhenReceiveCreateEventThenRecipientWillBeCreated() {
        UUID uuid = UUID.randomUUID();
        UserEvent event = new UserEvent();
        event.setId(uuid);
        event.setType(RecipientType.INDIVIDUAL_PERSON);
        event.setFullName("Nikolay Pletnev");
        event.setEmail("n.pletnev@alta-group.ru");
        event.setPhone("71231231212");
        event.setEvent(RecipientEventType.CREATE.name());
        recipientService.listenUserEvent(event);

        Assertions.assertTrue(recipientRepository.existsById(uuid));
        Assertions.assertEquals(2, recipientRepository.findAll().size());
    }

    @Test
    public void testWhenReceiveCreateEventAndRecipientAlreadyExistThenIgnored() {
        UserEvent event = new UserEvent();
        event.setEvent("CREATE");
        event.setId(id);
        event.setType(RecipientType.INDIVIDUAL_PERSON);
        recipientService.listenUserEvent(event);
        Assertions.assertTrue(recipientRepository.existsById(id));
        Assertions.assertEquals(1, recipientRepository.findAll().size());
    }

    @Test
    public void testWhenReceiveDeleteEventThenRecipientWillBeDeleted() {
        UserEvent event = new UserEvent();
        event.setId(id);
        event.setType(recipient.getType());
        event.setFullName(recipient.getFullName());
        event.setEmail(recipient.getEmail());
        event.setPhone(recipient.getPhone());
        event.setEvent(RecipientEventType.DELETE.name());
        recipientService.listenUserEvent(event);
        Assertions.assertFalse(recipientRepository.existsById(id));
        Assertions.assertEquals(0, recipientRepository.findAll().size());
    }

    @Test
    @Transactional
    public void testWhenReceiveDeleteEventThenStationHasOwnerIdNull() {
        UserEvent event = new UserEvent();
        event.setId(id);
        event.setType(recipient.getType());
        event.setFullName(recipient.getFullName());
        event.setEmail(recipient.getEmail());
        event.setPhone(recipient.getPhone());
        event.setEvent(RecipientEventType.DELETE.name());
        List<Station> stations = stationRepository.findByRecipient_Id(id);
        recipientService.listenUserEvent(event);
        Optional<Station> station1 = stationRepository.findById(stationId);
        Assertions.assertFalse(recipientRepository.existsById(id));
        Assertions.assertNull(stations.get(0).getRecipient());
        Assertions.assertTrue(station1.isPresent());
        Assertions.assertNull(station1.get().getRecipient());
    }

    @Test
    public void testWhenReceiveDeleteEventAndRecipientNotFound() {
        UUID uuid = UUID.randomUUID();
        UserEvent event = new UserEvent();
        event.setId(uuid);
        event.setType(RecipientType.INDIVIDUAL_PERSON);
        event.setFullName("Nikolay Pletnev");
        event.setEmail("n.pletnev@alta-group.ru");
        event.setPhone("71231231212");
        event.setEvent(RecipientEventType.DELETE.name());
        recipientService.listenUserEvent(event);

        Assertions.assertFalse(recipientRepository.existsById(uuid));
        Assertions.assertEquals(1, recipientRepository.findAll().size());
    }

    @Test
    public void testWhenReceiveUnknownEventThenIgnore() {
        UUID uuid = UUID.randomUUID();
        UserEvent event = new UserEvent();
        event.setId(uuid);
        event.setType(RecipientType.INDIVIDUAL_PERSON);
        event.setFullName("Nikolay Pletnev");
        event.setEmail("n.pletnev@alta-group.ru");
        event.setPhone("71231231212");
        event.setEvent("TEST");
        recipientService.listenUserEvent(event);

        Assertions.assertEquals(1, recipientRepository.findAll().size());
    }

    @TestConfiguration
    static class RecipientServiceComponentTestConfiguration {

        @Bean
        public RecipientService recipientService(RecipientRepository recipientRepository, DndRepository dndRepository,
                                                 NoticeSettingRepository noticeSettingRepository, StationRepository stationRepository) {
            return new RecipientService(recipientRepository, dndRepository, noticeSettingRepository, stationRepository);
        }
    }
}
