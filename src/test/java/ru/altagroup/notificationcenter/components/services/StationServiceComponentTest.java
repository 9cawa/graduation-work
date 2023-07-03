package ru.altagroup.notificationcenter.components.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.entities.RecipientType;
import ru.altagroup.notificationcenter.entities.Station;
import ru.altagroup.notificationcenter.events.StationBindingEvent;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;
import ru.altagroup.notificationcenter.repositories.StationRepository;
import ru.altagroup.notificationcenter.services.StationService;

import java.util.Optional;
import java.util.UUID;

@DataJpaTest
public class StationServiceComponentTest {

    @Autowired
    private StationService stationService;
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private RecipientRepository recipientRepository;

    private final UUID stationId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();

    @BeforeEach
    public void setup() {
        Recipient recipient = new Recipient();
        recipient.setId(ownerId);
        recipient.setFullName("Aksenov Aleksandr");
        recipient.setType(RecipientType.INDIVIDUAL_PERSON);
        recipient.setEmail("a.aksenov@alta-group.ru");
        recipient.setPhone("79856213510");
        Recipient savedRecipient = recipientRepository.save(recipient);

        Station station = new Station();
        station.setId(stationId);
        station.setName("name1");
        station.setRecipient(savedRecipient);
        stationRepository.save(station);

        savedRecipient.getStations().add(station);
        recipientRepository.save(savedRecipient);
    }

    @Test
    public void testWhenReceiveBindEventThenStationWillBeCreated() {
        UUID stationId2 = UUID.randomUUID();

        StationBindingEvent event = new StationBindingEvent();
        event.setName("name2");
        event.setStationId(stationId2);
        event.setOwnerId(ownerId);
        event.setEvent("BIND");
        stationService.listenStationEvent(event);
        Optional<Station> optionalStation = stationRepository.findById(stationId2);

        Assertions.assertTrue(optionalStation.isPresent());
        Assertions.assertEquals("name2", optionalStation.get().getName());
        Assertions.assertEquals(2, stationRepository.findAll().size());
    }

    @Test
    public void testWhenReceiveBindEventAndStationAlreadyExistsThenIgnore() {
        StationBindingEvent event = new StationBindingEvent();
        event.setName("name");
        event.setStationId(stationId);
        event.setOwnerId(ownerId);
        event.setEvent("BIND");
        stationService.listenStationEvent(event);

        Assertions.assertEquals(1, stationRepository.findAll().size());
    }

    @Test
    public void testWhenReceiveUnbindEventThenStationOwnerWillBeNull() {
        StationBindingEvent event = new StationBindingEvent();
        event.setName("name");
        event.setStationId(stationId);
        event.setOwnerId(null);
        event.setEvent("UNBIND");

        Optional<Station> optionalStation = stationRepository.findById(stationId);

        Assertions.assertNotNull(optionalStation.get().getRecipient().getId());

        stationService.listenStationEvent(event);
        Assertions.assertNull(optionalStation.get().getRecipient());
    }

    @Test
    public void testWhenReceiveUnbindEventAndStationAlreadyUnboundThenIgnore() {
        Optional<Station> optionalStation = stationRepository.findById(stationId);
        Assertions.assertTrue(optionalStation.isPresent());

        optionalStation.get().setRecipient(null);
        StationBindingEvent event = new StationBindingEvent();
        event.setName("name");
        event.setStationId(stationId);
        event.setOwnerId(null);
        event.setEvent("UNBIND");
        stationService.listenStationEvent(event);

        Assertions.assertSame(optionalStation.get(), stationRepository.findById(stationId).get());
        Assertions.assertEquals(1, stationRepository.findAll().size());
    }

    @Test
    public void testWhenReceiveUnbindEventAndStationNotFoundThenIgnore() {
        UUID notFoundStationId = UUID.randomUUID();
        StationBindingEvent event = new StationBindingEvent();
        event.setName("name");
        event.setStationId(notFoundStationId);
        event.setOwnerId(null);
        event.setEvent("UNBIND");
        stationService.listenStationEvent(event);
        Optional<Station> optionalStation = stationRepository.findById(notFoundStationId);

        Assertions.assertTrue(optionalStation.isEmpty());
        Assertions.assertEquals(1, stationRepository.findAll().size());
    }

    @Test
    public void testWhenReceiveUnknownEventThenIgnore() {
        StationBindingEvent event = new StationBindingEvent();
        event.setName("name");
        event.setStationId(stationId);
        event.setOwnerId(ownerId);
        event.setEvent("TEST");
        stationService.listenStationEvent(event);
        Optional<Station> optionalStation = stationRepository.findById(stationId);

        Assertions.assertTrue(optionalStation.isPresent());
        Assertions.assertEquals(ownerId, optionalStation.get().getRecipient().getId());
    }

    @TestConfiguration
    static class StationServiceComponentTestConfiguration {

        @Bean
        public StationService stationService(StationRepository stationRepository, RecipientRepository recipientRepository) {
            return new StationService(stationRepository, recipientRepository);
        }
    }
}
