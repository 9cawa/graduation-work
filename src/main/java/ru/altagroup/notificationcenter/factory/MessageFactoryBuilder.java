package ru.altagroup.notificationcenter.factory;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.altagroup.notificationcenter.entities.EventType;
import ru.altagroup.notificationcenter.exceptions.NotFoundException;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageFactoryBuilder {

    private final PasswordResetMessageFactory passwordResetMessageFactory;
    private final VerificationCodeMessageFactory verificationCodeMessageFactory;
    private final SludgeResetMessageFactory sludgeResetMessageFactory;
    private final HealthResetMessageFactory healthResetMessageFactory;
    private final ErrorMessageFactory errorMessageFactory;
    private final EquipmentReplacementFactory equipmentReplacementFactory;
    private final DealerSubscriptionMessageFactory dealerSubscriptionMessageFactory;
    private final DealerUnsubscriptionMessageFactory dealerUnsubscriptionMessageFactory;

    private HashMap<EventType, MessageFactory> createMap() {
        return new HashMap<>() {{
            put(EventType.ERROR, errorMessageFactory);
            put(EventType.HEALTH_RESET, healthResetMessageFactory);
            put(EventType.SLUDGE_RESET, sludgeResetMessageFactory);
            put(EventType.VERIFICATION_CODE, verificationCodeMessageFactory);
            put(EventType.PASSWORD_RESET, passwordResetMessageFactory);
            put(EventType.EQUIPMENT_REPLACEMENT, equipmentReplacementFactory);
            put(EventType.DEALER_SUBSCRIPTION, dealerSubscriptionMessageFactory);
            put(EventType.DEALER_UNSUBSCRIPTION, dealerUnsubscriptionMessageFactory);
        }};
    }

    public MessageFactory getMessageFactory(EventType eventType) {
        Map<EventType, MessageFactory> strategyMap = createMap();
        if (!strategyMap.containsKey(eventType))
            throw new NotFoundException("Message factory does not exist for event " + eventType.name());
        return strategyMap.get(eventType);
    }
}
