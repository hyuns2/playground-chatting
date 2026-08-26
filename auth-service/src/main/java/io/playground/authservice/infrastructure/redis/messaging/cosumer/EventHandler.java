package io.playground.authservice.infrastructure.redis.messaging.cosumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.authservice.application.EventService;
import io.playground.authservice.infrastructure.redis.messaging.constant.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventHandler {
    private final ObjectMapper objectMapper;
    private final EventService eventService;

    public void handleUserProfileCreatedSuccessEvent(EventEnvelope envelope) throws JsonProcessingException {
        UserProfileCreatedEvent event = objectMapper.readValue(envelope.getPayload(), UserProfileCreatedEvent.class);

        eventService.activateSignUp(event.userId());
    }

    public void handleUserProfileCreatedFailureEvent(EventEnvelope envelope) throws JsonProcessingException {
        UserProfileCreatedEvent event = objectMapper.readValue(envelope.getPayload(), UserProfileCreatedEvent.class);

        eventService.rollbackSignUp(event.userId());
    }
}
