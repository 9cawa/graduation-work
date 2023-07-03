package ru.altagroup.notificationcenter.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.altagroup.notificationcenter.entities.Station;

import java.util.List;
import java.util.UUID;

@Repository
public interface StationRepository extends JpaRepository<Station, UUID> {
    List<Station> findByRecipient_Id(UUID ownerId);
}
