package ru.altagroup.notificationcenter.configurations.filters;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import ru.altagroup.notificationcenter.entities.Dnd;
import ru.altagroup.notificationcenter.entities.Recipient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
public class DndFilter implements ChainFilter {

    private ChainFilter chain;

    @Override
    public void setNextChain(ChainFilter nextChain) {
        this.chain = nextChain;
    }

    @Override
    public boolean filter(Message<?> message) {
        Recipient recipient = message.getHeaders().get("recipient", Recipient.class);
        assert recipient != null;
        if (chain == null || doNotDisturb(LocalDateTime.now(), recipient.getDnd())) return false;
        return chain.filter(message);
    }

    //true -> не отправлять, false -> отправлять
    private boolean doNotDisturb(LocalDateTime dateTime, Dnd dnd) {
        if (!dnd.isActive() && (dnd.getStartTime() == null || dnd.getEndTime() == null)) return false;
        ZoneId userZoneId = ZoneId.of(dnd.getZoneId());
        LocalDateTime now = dateTime.atZone(userZoneId).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime begin = LocalDateTime.of(LocalDate.now(), dnd.getStartTime()).atZone(userZoneId).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), dnd.getEndTime()).atZone(userZoneId).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        if (begin.isAfter(end) || begin.isEqual(end)) end = end.plusDays(1);
        return now.isAfter(begin) && now.isBefore(end);
    }
}
