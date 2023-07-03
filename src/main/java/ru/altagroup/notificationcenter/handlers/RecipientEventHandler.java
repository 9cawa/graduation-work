package ru.altagroup.notificationcenter.handlers;

public interface RecipientEventHandler {
    void handle(ru.altacloud.v2.avro.UserEvent event);
}
