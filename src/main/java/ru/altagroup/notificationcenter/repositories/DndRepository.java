package ru.altagroup.notificationcenter.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.altagroup.notificationcenter.entities.Dnd;

import java.util.Optional;
import java.util.UUID;

public interface DndRepository extends JpaRepository<Dnd, UUID> {
    Optional<Dnd> findByRecipient_Id(UUID id);
}
