package io.playground.chatservice.infrastructure.kafka;

import io.playground.chatservice.infrastructure.outbox.OutboxEntity;

import java.time.Instant;

public record EventEnvelope(
        String eventId,
        String eventType,
        Instant occurredAt,
        String traceId,
        String partitionKey,
        String payload
) {
    public static EventEnvelope from(OutboxEntity event) {
        return new EventEnvelope(
                event.getEventId(),
                event.getEventType().name(),
                event.getOccurredAt(),
                event.getTraceId(),
                event.getPartitionKey(),
                event.getPayload()
        );
    }
}
