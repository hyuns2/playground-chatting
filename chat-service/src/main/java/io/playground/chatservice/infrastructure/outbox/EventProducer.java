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
    public <T> void produce(ChatEventDto.EventType eventType,
                            Instant occurredAt,
                            String traceId,
                            String partitionKey,
                            T payload) {
        outboxRepository.save(
                OutboxEntity.of(
                        UUID.randomUUID().toString(),
                        eventType,
                        occurredAt,
                        traceId,
                        partitionKey,
                        objectMapper.writeValueAsString(payload),
                        false
                    )
        );
    }
}
