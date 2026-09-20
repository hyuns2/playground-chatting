package io.playground.chatservice.application.event;

import java.time.Instant;

public interface EventProducerPort {
    <T> void produce(ChatEventDto.EventType eventType,
                     Instant occurredAt,
                     String traceId,
                     String partitionKey,
                     T payload);
}
