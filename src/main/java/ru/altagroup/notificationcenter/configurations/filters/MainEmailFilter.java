package ru.altagroup.notificationcenter.configurations.filters;

import lombok.AllArgsConstructor;
import org.springframework.integration.annotation.Filter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import ru.altagroup.notificationcenter.dto.MessageDto;
import ru.altagroup.notificationcenter.entities.EventType;

@AllArgsConstructor
@Component
public class MainEmailFilter {

    private final DndFilter dndFilter;
    private final EmailMessageFilter emailFilter;
    private final EmailFrequencyFilter emailFrequencyFilter;

    @Filter
    public boolean accept(Message<?> message) {
        MessageDto messageDto = (MessageDto) message.getPayload();
        EventType type = messageDto.getEventType();
        if (type.equals(EventType.PASSWORD_RESET)) return true;
        ChainFilter chainFilter = getChainFilter();
        return chainFilter.filter(message);
    }

    private ChainFilter getChainFilter() {
        dndFilter.setNextChain(emailFilter);
        emailFilter.setNextChain(emailFrequencyFilter);
        return dndFilter;
    }
}
