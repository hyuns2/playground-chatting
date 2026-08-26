package io.playground.authservice.infrastructure.redis.messaging.constant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEnvelope {
    private String eventId;
    private String eventType;
    private String payload;

    public static EventEnvelope of(String eventType, String payload) {
        return EventEnvelope.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .payload(payload)
                .build();
    }

    public Map<String, String> toMap() {
        return Map.of(
                "eventId", eventId,
                "eventType", eventType,
                "payload", payload
        );
    }
}
