package io.playground.chatservice.infrastructure.outbox;

import io.playground.chatservice.application.event.ChatEventDto;
import io.playground.chatservice.infrastructure.kafka.EventEnvelope;
import io.playground.chatservice.infrastructure.kafka.KafkaEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaEventProducer kafkaEventProducer;

    private OutboxService sut;

    @BeforeEach
    void setUp() {
        sut = new OutboxService(outboxRepository, kafkaEventProducer);
    }

    private static OutboxEntity outboxEntity(Long id) {
        return OutboxEntity.builder()
                .id(id)
                .eventId("event-" + id)
                .eventType(ChatEventDto.EventType.CHAT_MESSAGE_SENT)
                .occurredAt(Instant.now())
                .traceId("trace-" + id)
                .payload("{}")
                .processed(false)
                .build();
    }

    @Nested
    @DisplayName("produceAllUnprocessedEvents")
    class ProduceAllUnprocessedEvents {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("미처리 이벤트마다 카프카로 전송하고, id별 전송 결과 Future를 맵으로 반환한다")
        void producesEachUnprocessedEventAndReturnsFuturesById() {
            OutboxEntity entity1 = outboxEntity(1L);
            OutboxEntity entity2 = outboxEntity(2L);
            when(outboxRepository.findAllByProcessedOrderByOccurredAtAsc(false))
                    .thenReturn(List.of(entity1, entity2));

            CompletableFuture<SendResult<String, Object>> future1 =
                    CompletableFuture.completedFuture(mock(SendResult.class));
            CompletableFuture<SendResult<String, Object>> future2 = new CompletableFuture<>();
            when(kafkaEventProducer.send(any(EventEnvelope.class))).thenReturn(future1, future2);

            Map<Long, CompletableFuture<?>> result = sut.produceAllUnprocessedEvents();

            assertThat(result).hasSize(2);
            assertThat(result.get(1L)).isSameAs(future1);
            assertThat(result.get(2L)).isSameAs(future2);
            verify(kafkaEventProducer, times(2)).send(any());
        }

        @Test
        @DisplayName("미처리 이벤트가 없으면 빈 맵을 반환하고 카프카로 전송하지 않는다")
        void returnsEmptyMapWhenNothingUnprocessed() {
            when(outboxRepository.findAllByProcessedOrderByOccurredAtAsc(false))
                    .thenReturn(List.of());

            Map<Long, CompletableFuture<?>> result = sut.produceAllUnprocessedEvents();

            assertThat(result).isEmpty();
            verify(kafkaEventProducer, never()).send(any());
        }
    }

    @Nested
    @DisplayName("afterProducing")
    class AfterProducing {

        @Test
        @DisplayName("성공 id가 있으면 processed 상태를 true로 일괄 갱신한다")
        void marksSuccessfulIdsAsProcessed() {
            sut.afterProducing(List.of(1L, 2L));

            verify(outboxRepository).updateProcessedTrueByIds(List.of(1L, 2L));
        }

        @Test
        @DisplayName("성공 id가 없으면 갱신 쿼리를 실행하지 않는다")
        void doesNothingWhenNoSuccessfulIds() {
            sut.afterProducing(List.of());

            verify(outboxRepository, never()).updateProcessedTrueByIds(any());
        }
    }
}
