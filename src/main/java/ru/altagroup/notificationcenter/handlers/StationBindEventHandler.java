package ru.altagroup.notificationcenter.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.entities.Station;
import ru.altagroup.notificationcenter.events.StationBindingEvent;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;
import ru.altagroup.notificationcenter.repositories.StationRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StationBindEventHandler implements StationEventHandler {

    private final StationRepository stationRepository;
    private final RecipientRepository recipientRepository;

    @Override
    public void handle(StationBindingEvent event) {
        Optional<Recipient> recipient = recipientRepository.findById(event.getOwnerId());
        if (recipient.isPresent()) {
            Station station = new Station();
            station.setId(event.getStationId());
            station.setName(event.getName());
            station.setRecipient(recipient.get());

            stationRepository.save(station);
        }
    }
}
