package io.playground.chatservice.application.event;

import java.time.Instant;

public interface EventProducerPort {
    <T> void produce(String traceId,
                     ChatEventDto.EventType eventType,
                     Instant occurredAt,
                     T payload);
}
