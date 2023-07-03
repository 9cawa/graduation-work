package ru.altagroup.notificationcenter.components.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.entities.RecipientType;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;

import java.util.UUID;

@DataJpaTest
public class RecipientRepositoryTest {

    @Autowired
    private RecipientRepository recipientRepository;

    private UUID recipientId = UUID.randomUUID();

    @BeforeEach
    public void setup() {
        recipientRepository.save(recipient());
    }

    private Recipient recipient() {
        Recipient recipient = new Recipient();
        recipient.setId(recipientId);
        recipient.setType(RecipientType.INDIVIDUAL_PERSON);
        recipient.setFullName("Aleksandr Aksenov");
        recipient.setEmail("a.aksenov@alta-group.ru");
        recipient.setPhone("79856213510");

        return recipient;
    }

    @AfterEach
    public void tearDown() {
        if (recipientRepository.existsById(recipientId)) recipientRepository.deleteById(recipientId);
    }

    @Test
    public void testRecipientExists() {
        Recipient recipient = recipientRepository.findById(recipientId).get();
        Assertions.assertEquals(recipient.getEmail(), "a.aksenov@alta-group.ru");
        Assertions.assertNotNull(recipient.getId());
    }

    @Test
    public void testDeleteRecipient() {
        recipientRepository.deleteById(recipientId);
        Assertions.assertEquals(recipientRepository.count(), 0);
    }

}
