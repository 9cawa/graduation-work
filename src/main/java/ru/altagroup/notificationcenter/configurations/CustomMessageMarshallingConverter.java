package ru.altagroup.notificationcenter.configurations;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.stereotype.Component;

@Component
public class CustomMessageMarshallingConverter extends MappingJackson2MessageConverter {
}
