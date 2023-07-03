package ru.altagroup.notificationcenter.configurations.filters;

import org.springframework.messaging.Message;

public interface ChainFilter {

    void setNextChain(ChainFilter nextChain);

    boolean filter(Message<?> message);
}
