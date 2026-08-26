package io.playground.userservice.adapter.outbox;

import io.playground.userservice.domain.event.EventType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "EVENT_LOG")
public class EventLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String eventId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(nullable = false)
    @Lob
    private String payload;

    @Column(nullable = false)
    private boolean processed;

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
