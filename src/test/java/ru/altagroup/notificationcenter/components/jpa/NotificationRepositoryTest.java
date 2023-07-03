package ru.altagroup.notificationcenter.components.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.altagroup.notificationcenter.entities.*;
import ru.altagroup.notificationcenter.repositories.NotificationRepository;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;
import ru.altagroup.notificationcenter.repositories.StationRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private StationRepository stationRepository;

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
        stationRepository.deleteAll();
        notificationRepository.deleteAll();
        recipientRepository.deleteAll();
    }

    @Test
    public void testCreateNotification() {
        Notification notification = new Notification();
        Recipient recipient = recipientRepository.getReferenceById(ownerId);
        notification.setRecipient(recipient);

        Notification save = notificationRepository.save(notification);

        Assertions.assertEquals(notification.getId(), save.getId());
    }

    @Test
    public void testDeleteNotification() {
        Notification notification = new Notification();
        Recipient recipient = recipientRepository.getReferenceById(ownerId);
        notification.setRecipient(recipient);

        Notification save = notificationRepository.save(notification);

        Assertions.assertEquals(1, notificationRepository.count());

        notificationRepository.delete(save);

        Assertions.assertEquals(0, notificationRepository.count());
    }

    @Test
    public void testFindByRecipient_IdAndNoticeAndChannelOrderByTimestampDesc() {
        Recipient recipient = recipientRepository.getReferenceById(ownerId);

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setChannel(Channel.EMAIL);
        notification.setTimestamp((LocalDateTime.now().plusDays(1)).toInstant(ZoneOffset.UTC).toEpochMilli());
        notification.setNotice(Notice.TEMPERATURE);
        notificationRepository.save(notification);

        Notification notification1 = new Notification();
        notification1.setRecipient(recipient);
        notification.setChannel(Channel.EMAIL);
        notification1.setTimestamp((LocalDateTime.now().minusDays(1)).toInstant(ZoneOffset.UTC).toEpochMilli());
        notification1.setNotice(Notice.SYSTEM);
        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setRecipient(recipient);
        notification2.setChannel(Channel.EMAIL);
        notification2.setTimestamp(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        notification2.setNotice(Notice.SYSTEM);
        notificationRepository.save(notification2);

        Notification notification3 = new Notification();
        notification3.setRecipient(recipient);
        notification3.setChannel(Channel.SMS);
        notification3.setTimestamp(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        notification3.setNotice(Notice.SYSTEM);
        notificationRepository.save(notification3);

        Optional<Notification> result = notificationRepository
                .findFirstByRecipient_IdAndNoticeAndChannelOrderByTimestampDesc(ownerId, Notice.SYSTEM, Channel.EMAIL);
        Optional<Notification> result2 = notificationRepository
                .findFirstByRecipient_IdAndNoticeAndChannelOrderByTimestampDesc(ownerId, Notice.SYSTEM, Channel.SMS);

//        System.out.println(DateFormat.getDateInstance(DateFormat.SHORT).format(result.getTimestamp()));
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(notification2.getId() ,result.get().getId());
        Assertions.assertTrue(result2.isPresent());
        Assertions.assertEquals(notification3.getId() ,result2.get().getId());
    }

    @Test
    public void testFindFirst3ByRecipient_IdAndEventOrderByTimestampDesc() {
        Recipient recipient = recipientRepository.getReferenceById(ownerId);

        Notification notification1 = new Notification();
        notification1.setRecipient(recipient);
        notification1.setTimestamp((LocalDateTime.now().minusHours(1)).toInstant(ZoneOffset.UTC).toEpochMilli());
        notification1.setNotice(Notice.SYSTEM);
        notification1.setEvent(EventType.VERIFICATION_CODE);
        notificationRepository.save(notification1);

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTimestamp((LocalDateTime.now()).toInstant(ZoneOffset.UTC).toEpochMilli());
        notification.setNotice(Notice.EMPTY);
        notification.setEvent(EventType.VERIFICATION_CODE);
        notificationRepository.save(notification);

        Notification notification2 = new Notification();
        notification2.setRecipient(recipient);
        notification2.setTimestamp((LocalDateTime.now().minusHours(2)).toInstant(ZoneOffset.UTC).toEpochMilli());
        notification2.setNotice(Notice.PUMP_RESOURCE);
        notification2.setEvent(EventType.VERIFICATION_CODE);
        notificationRepository.save(notification2);

        Notification notification3 = new Notification();
        notification3.setRecipient(recipient);
        notification3.setTimestamp((LocalDateTime.now().minusDays(1)).toInstant(ZoneOffset.UTC).toEpochMilli());
        notification3.setNotice(Notice.EMPTY);
        notification3.setEvent(EventType.VERIFICATION_CODE);
        notificationRepository.save(notification3);

        Optional<List<Notification>> result = notificationRepository.findFirst3ByRecipient_IdAndEventOrderByTimestampDesc(ownerId, EventType.VERIFICATION_CODE);
        Assertions.assertTrue(result.isPresent());
        LocalDateTime date1 = Instant.ofEpochMilli(result.get().get(0).getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime date2 = Instant.ofEpochMilli(result.get().get(1).getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        Assertions.assertTrue(date1.isAfter(date2));
        Assertions.assertEquals(result.get().size(), 3);
    }

    @Test
    public void testFindFirst3ByRecipient_IdAndEventOrderByTimestampDescWhenOnlyOneVerificationCodeNotification() {
        Recipient recipient = recipientRepository.getReferenceById(ownerId);

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTimestamp((LocalDateTime.now()).toInstant(ZoneOffset.UTC).toEpochMilli());
        notification.setNotice(Notice.EMPTY);
        notification.setEvent(EventType.VERIFICATION_CODE);
        notificationRepository.save(notification);

        Notification notification1 = new Notification();
        notification1.setRecipient(recipient);
        notification1.setTimestamp((LocalDateTime.now()).toInstant(ZoneOffset.UTC).toEpochMilli());
        notification1.setNotice(Notice.EMPTY);
        notification1.setEvent(EventType.HEALTH_RESET);
        notificationRepository.save(notification1);

        Optional<List<Notification>> result = notificationRepository.findFirst3ByRecipient_IdAndEventOrderByTimestampDesc(ownerId, EventType.VERIFICATION_CODE);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(result.get().size(), 1);
    }

    @Test
    public void testFindFirst3ByRecipient_IdAndEventOrderByTimestampDescWhenZeroVerificationCodeNotifications() {
        Recipient recipient = recipientRepository.getReferenceById(ownerId);

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTimestamp((LocalDateTime.now()).toInstant(ZoneOffset.UTC).toEpochMilli());
        notification.setNotice(Notice.EMPTY);
        notification.setEvent(EventType.HEALTH_RESET);
        notificationRepository.save(notification);

        Optional<List<Notification>> result = notificationRepository.findFirst3ByRecipient_IdAndEventOrderByTimestampDesc(ownerId, EventType.VERIFICATION_CODE);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue(result.get().size() < 3);
    }
}
