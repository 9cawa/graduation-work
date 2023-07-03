package ru.altagroup.notificationcenter.handlers;

import ru.altagroup.notificationcenter.events.StationBindingEvent;

public interface StationEventHandler {
    void handle(StationBindingEvent event);
}
