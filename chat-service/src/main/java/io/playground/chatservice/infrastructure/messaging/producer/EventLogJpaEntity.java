package io.playground.chatservice.infrastructure.messaging.producer;

import io.playground.chatservice.application.eventstream.PubEventType;
import io.playground.chatservice.infrastructure.messaging.EventEnvelope;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "EVENT_LOG")
public class EventLogJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String eventId;

    @Column
    private String streamKey;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PubEventType eventType;

    @Column(nullable = false)
    @Lob
    private String payload;

    @Column(nullable = false)
    private boolean processed;

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public static EventLogJpaEntity of(String streamKey,
                                       PubEventType type,
                                       String payload) {
        return new EventLogJpaEntity(
                null,
                streamKey,
                type,
                payload,
                false
        );
    }

    public EventEnvelope toEventEnvelope() {
        return EventEnvelope.of(
                this.eventId,
                this.eventType.getValue(),
                this.payload
        );
    }
}
