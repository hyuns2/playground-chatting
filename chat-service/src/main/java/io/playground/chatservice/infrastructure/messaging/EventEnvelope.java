package io.playground.chatservice.infrastructure.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope {
    private String eventId;
    private String eventType;
    private String payload;

    public static EventEnvelope of(String eventId,
                                   String eventType,
                                   String payload) {
        return new EventEnvelope(eventId, eventType, payload);
    }

    public Map<String, String> toMap() {
        return Map.of(
                "eventId", eventId,
                "eventType", eventType,
                "payload", payload
        );
    }
}
