package ru.altagroup.notificationcenter.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.altagroup.notificationcenter.entities.NoticeSetting;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.entities.Station;
import ru.altagroup.notificationcenter.events.UserEvent;
import ru.altagroup.notificationcenter.repositories.DndRepository;
import ru.altagroup.notificationcenter.repositories.NoticeSettingRepository;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;
import ru.altagroup.notificationcenter.repositories.StationRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RecipientDeleteEventHandler implements RecipientEventHandler {

    private final RecipientRepository recipientRepository;
    private final StationRepository stationRepository;
    private final DndRepository dndRepository;
    private final NoticeSettingRepository noticeSettingRepository;

    @Override
    public void handle(UserEvent event) {
        UUID id = event.getId();
        Optional<Recipient> recipient = recipientRepository.findById(id);
        if (recipient.isPresent()) {
            List<Station> stations = stationRepository.findByRecipient_Id(id);
            stations.forEach(s -> s.setRecipient(null));
            stationRepository.saveAll(stations);
            dndRepository.delete(recipient.get().getDnd());
            List<NoticeSetting> noticeSettings = recipient.get().getNoticeSettings();
            noticeSettingRepository.deleteAll(noticeSettings);
            recipientRepository.deleteById(id);
        }
    }
}
