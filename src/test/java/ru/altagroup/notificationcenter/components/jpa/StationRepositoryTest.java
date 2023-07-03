package ru.altagroup.notificationcenter.components.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.entities.RecipientType;
import ru.altagroup.notificationcenter.entities.Station;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;
import ru.altagroup.notificationcenter.repositories.StationRepository;

import java.util.List;
import java.util.UUID;

@DataJpaTest
public class StationRepositoryTest {
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private RecipientRepository recipientRepository;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID stationId = UUID.randomUUID();


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

    @AfterEach
    public void tearDown() {
        if (stationRepository.existsById(stationId)) stationRepository.deleteById(stationId);
    }

    @Test
    public void testStationExists() {
        Station station = stationRepository.findById(stationId).get();
        Assertions.assertEquals("name1", station.getName());
        Assertions.assertNotNull(station.getId());
    }

    @Test
    public void testDeleteStation() {
        stationRepository.deleteById(stationId);
        Assertions.assertEquals(0, stationRepository.count());
    }

    @Test
    public void testFindByOwnerId() {
        List<Station> stations = stationRepository.findByRecipient_Id(ownerId);
        Assertions.assertEquals(1, stations.size());
        Assertions.assertEquals("name1", stations.get(0).getName());
        Recipient recipient = recipientRepository.findById(ownerId).get();
        Assertions.assertEquals(1, recipient.getStations().size());
    }

}
