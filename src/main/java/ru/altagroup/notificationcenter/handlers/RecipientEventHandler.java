package ru.altagroup.notificationcenter.handlers;

import ru.altagroup.notificationcenter.events.UserEvent;

public interface RecipientEventHandler {
    void handle(UserEvent event);
}
