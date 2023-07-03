package ru.altagroup.notificationcenter.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.altagroup.notificationcenter.entities.Dnd;
import ru.altagroup.notificationcenter.entities.Notice;
import ru.altagroup.notificationcenter.entities.NoticeSetting;
import ru.altagroup.notificationcenter.entities.Recipient;
import ru.altagroup.notificationcenter.events.UserEvent;
import ru.altagroup.notificationcenter.repositories.DndRepository;
import ru.altagroup.notificationcenter.repositories.NoticeSettingRepository;
import ru.altagroup.notificationcenter.repositories.RecipientRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecipientCreateEventHandler implements RecipientEventHandler {

    private final RecipientRepository recipientRepository;
    private final DndRepository dndRepository;
    private final NoticeSettingRepository noticeSettingRepository;

    @Override
    public void handle(UserEvent event) {
        if (!recipientRepository.existsById(event.getId())) {
            Recipient recipient = new Recipient();
            recipient.setId(event.getId());
            recipient.setFullName(event.getFullName());
            recipient.setType(event.getType());
            recipient.setEmail(event.getEmail());
            recipient.setPhone(event.getPhone());

            Recipient saved = recipientRepository.save(recipient);
            saved.setDnd(defaultDndSettings());
            saved.setNoticeSettings(defaultNoticeSettings());
            recipientRepository.save(saved);
        }
    }

    private Dnd defaultDndSettings() {
        Dnd dnd = new Dnd();
        dnd.setActive(true);
        dnd.setStartTime(LocalTime.of(22, 0));
        dnd.setEndTime(LocalTime.of(8,30));
        return dndRepository.save(dnd);
    }

    private List<NoticeSetting> defaultNoticeSettings() {
        List<NoticeSetting> settings = new ArrayList<>();

        NoticeSetting system = new NoticeSetting();
        system.setNotice(Notice.SYSTEM);
        NoticeSetting savedSystem = noticeSettingRepository.save(system);
        settings.add(savedSystem);

        NoticeSetting sludgeLvl = new NoticeSetting();
        sludgeLvl.setNotice(Notice.SLUDGE_LEVEL);
        NoticeSetting savedSludgeLvl = noticeSettingRepository.save(sludgeLvl);
        settings.add(savedSludgeLvl);

        NoticeSetting temperature = new NoticeSetting();
        temperature.setNotice(Notice.TEMPERATURE);
        NoticeSetting savedTemperature = noticeSettingRepository.save(temperature);
        settings.add(savedTemperature);

        NoticeSetting pumpResource = new NoticeSetting();
        pumpResource.setNotice(Notice.PUMP_RESOURCE);
        NoticeSetting savedPumpResource = noticeSettingRepository.save(pumpResource);
        settings.add(savedPumpResource);

        return settings;
    }
}
