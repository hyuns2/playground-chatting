package io.playground.chatservice.infrastructure.outbox;

import io.playground.chatservice.application.event.ChatEventDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "outboxes",
        indexes = {
                @Index(
                        name = "idx_processed_occurredAt",
                        columnList = "processed, occurredAt"
                )
        }
)
public class OutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String eventId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatEventDto.EventType eventType;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private String traceId;

    @Column
    private String partitionKey;

    @Column(columnDefinition = "json", nullable = false)
    private String payload;

    // ToDo: 운영에서는 retryCount와 lockedUntil도 같이 관리하거나, CDC 방식으로 변환 필요
    @Column(nullable = false)
    private boolean processed;
}
