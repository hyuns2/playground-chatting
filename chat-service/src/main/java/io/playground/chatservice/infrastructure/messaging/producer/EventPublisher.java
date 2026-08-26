package io.playground.chatservice.infrastructure.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.chatservice.application.chat.port.EventPublisherPort;
import io.playground.chatservice.application.eventstream.PubEventType;
import io.playground.chatservice.exception.CustomErrorCode;
import io.playground.chatservice.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher implements EventPublisherPort {
    private final EventLogJpaRepository eventLogJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public <T> void publish(PubEventType type, String streamKey, T payload) {
        handle(type, streamKey, payload);
    }

    private <T> void handle(PubEventType eventType, String streamKey, T payload) {
        try {
            eventLogJpaRepository.save(
                    EventLogJpaEntity.of(
                            streamKey,
                            eventType,
                            objectMapper.writeValueAsString(payload)
                    )
            );
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.PRODUCING_FAILED);
        }
    }
}
