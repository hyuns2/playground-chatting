package io.playground.userservice.adapter.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEnvelope {
    private String eventId;
    private String eventType;
    private String payload;

    public Map<String, String> toMap() {
        return Map.of(
                "eventId", eventId,
                "eventType", eventType,
                "payload", payload
        );
    }
}
