package ru.altagroup.notificationcenter.handlers;

import ru.altacloud.v2.avro.StationBindingEvent;

public interface StationEventHandler {
    void handle(StationBindingEvent event);
}
