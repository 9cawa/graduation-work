package ru.altagroup.notificationcenter.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.altagroup.notificationcenter.entities.EventType;
import ru.altagroup.notificationcenter.exceptions.NotFoundException;

import java.util.HashMap;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationStrategySelector {

    private final HealthResetNotificationStrategy healthResetNotificationStrategy;
    private final ErrorNotificationStrategy errorNotificationStrategy;
    private final EquipmentReplacementNotificationStrategy equipmentReplacementNotificationStrategy;
    private final SludgeResetNotificationStrategy sludgeResetNotificationStrategy;
    private final VerificationCodeNotificationStrategy verificationCodeNotificationStrategy;
    private final ResetPasswordNotificationStrategy resetPasswordNotificationStrategy;
    private final DealerSubscriptionNotificationStrategy dealerSubscriptionNotificationStrategy;

    public NotificationStrategy selectStrategyByEventType(EventType eventType) {
        Optional<NotificationStrategy> optionalStrategy = Optional.ofNullable(strategyMap().get(eventType));
        return optionalStrategy.orElseThrow(() -> new NotFoundException("Strategy not found by event type: " + eventType.name()));
    }

    private HashMap<EventType, NotificationStrategy> strategyMap() {
        return new HashMap<>() {{
            put(EventType.EQUIPMENT_REPLACEMENT, equipmentReplacementNotificationStrategy);
            put(EventType.HEALTH_RESET, healthResetNotificationStrategy);
            put(EventType.SLUDGE_RESET, sludgeResetNotificationStrategy);
            put(EventType.ERROR, errorNotificationStrategy);
            put(EventType.VERIFICATION_CODE, verificationCodeNotificationStrategy);
            put(EventType.PASSWORD_RESET, resetPasswordNotificationStrategy);
            put(EventType.DEALER_SUBSCRIPTION, dealerSubscriptionNotificationStrategy);
            put(EventType.DEALER_UNSUBSCRIPTION, dealerSubscriptionNotificationStrategy);
        }};
    }
}
