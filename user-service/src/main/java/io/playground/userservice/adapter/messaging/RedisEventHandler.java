package io.playground.userservice.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.userservice.application.service.EventUsecase;
import io.playground.userservice.domain.event.AuthSignUpEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RedisEventHandler {
    private final ObjectMapper objectMapper;
    private final EventUsecase eventUsecase;

    public void handleAuthSignUpEvent(EventEnvelope envelope) throws JsonProcessingException {
        eventUsecase.handleAuthSignUpEvent(
                envelope.getEventId(),
                // ToDo: 추후 envelope에 createdAt 추가 시, 변경 필요
                LocalDateTime.now(),
                objectMapper.readValue(envelope.getPayload(), AuthSignUpEvent.class)
        );
    }
}
