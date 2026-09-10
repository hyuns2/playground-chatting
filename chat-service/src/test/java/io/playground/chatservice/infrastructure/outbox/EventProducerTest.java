package io.playground.chatservice.infrastructure.outbox;

import io.playground.chatservice.application.event.ChatEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventProducerTest {

    @Mock
    private OutboxRepository outboxRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private EventProducer sut;

    @BeforeEach
    void setUp() {
        sut = new EventProducer(outboxRepository, objectMapper);
    }

    @Test
    @DisplayName("produce는 페이로드를 JSON으로 직렬화해 미처리 상태의 outbox row로 저장한다")
    void savesOutboxEntity() {
        when(outboxRepository.save(org.mockito.ArgumentMatchers.any(OutboxEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant occurredAt = Instant.parse("2026-01-01T00:00:00Z");
        ChatEventDto.ChatMessageSent payload = new ChatEventDto.ChatMessageSent(
                100L, 10L, 1L, io.playground.chatservice.domain.ChatMessage.MessageType.TEXT,
                "hello", null, occurredAt
        );

        sut.produce("trace-1", ChatEventDto.EventType.CHAT_MESSAGE_SENT, occurredAt, payload);

        ArgumentCaptor<OutboxEntity> captor = ArgumentCaptor.forClass(OutboxEntity.class);
        verify(outboxRepository).save(captor.capture());
        OutboxEntity saved = captor.getValue();

        assertThat(saved.getEventId()).isNotBlank();
        assertThat(UUID.fromString(saved.getEventId())).isNotNull();
        assertThat(saved.getEventType()).isEqualTo(ChatEventDto.EventType.CHAT_MESSAGE_SENT);
        assertThat(saved.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(saved.getTraceId()).isEqualTo("trace-1");
        assertThat(saved.isProcessed()).isFalse();
        assertThat(saved.getPayload()).isEqualTo(objectMapper.writeValueAsString(payload));
    }
}
