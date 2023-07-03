package ru.altagroup.notificationcenter.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.altacloud.v2.avro.StationBindingEvent;
import ru.altagroup.notificationcenter.entities.Station;
import ru.altagroup.notificationcenter.repositories.StationRepository;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StationUnbindEventHandler implements StationEventHandler {

    private final StationRepository stationRepository;

    @Override
    public void handle(StationBindingEvent event) {
        UUID id = event.getStationId();
        Optional<Station> station = stationRepository.findById(id);
        if (station.isPresent()) {
            Station stationToSave = station.get();
            stationToSave.setId(event.getStationId());
            stationToSave.setName(event.getName().toString());
            stationToSave.setRecipient(null);
            stationRepository.save(station.get());
        }

    }
}
