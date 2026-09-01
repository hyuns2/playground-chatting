package io.playground.chatservice.infrastructure.outbox;

import io.playground.chatservice.application.event.ChatEventDto;
import io.playground.chatservice.application.event.EventProducerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventProducer implements EventProducerPort {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public <T> void produce(String traceId,
                            ChatEventDto.EventType eventType,
                            Instant occurredAt,
                            T payload) {
        outboxRepository.save(
                OutboxEntity.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType(eventType)
                        .occurredAt(occurredAt)
                        .traceId(traceId)
                        .payload(
                                objectMapper.writeValueAsString(payload)
                        )
                        .processed(false)
                        .build()
        );
    }
}
