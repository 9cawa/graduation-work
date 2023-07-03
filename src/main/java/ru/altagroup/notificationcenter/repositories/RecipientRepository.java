package ru.altagroup.notificationcenter.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.altagroup.notificationcenter.entities.Recipient;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, UUID> {

     Optional<Recipient> findByPhone(String phone);
     Optional<Recipient> findByEmail(String email);
}
