package ru.altagroup.notificationcenter.handlers;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.altagroup.notificationcenter.events.NotificationEvent;

public interface NotificationStrategy {

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    void notify(NotificationEvent notification);
}
