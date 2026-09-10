package io.playground.chatservice.infrastructure.scheduling;

import io.playground.chatservice.infrastructure.outbox.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPollerTest {

    @Mock
    private OutboxService outboxService;

    private OutboxPoller sut;

    @BeforeEach
    void setUp() {
        sut = new OutboxPoller(outboxService);
    }

    @Test
    @DisplayName("전송에 성공한 id만 모아 afterProducing에 전달한다")
    void passesOnlySuccessfulIdsToAfterProducing() {
        Map<Long, CompletableFuture<?>> results = new LinkedHashMap<>();
        results.put(1L, CompletableFuture.completedFuture("ok"));
        results.put(2L, CompletableFuture.failedFuture(new RuntimeException("kafka down")));
        when(outboxService.produceAllUnprocessedEvents()).thenReturn(results);

        sut.poll();

        verify(outboxService).afterProducing(List.of(1L));
    }

    @Test
    @DisplayName("미처리 이벤트가 없으면 afterProducing을 빈 목록으로 호출한다")
    void callsAfterProducingWithEmptyListWhenNothingToProduce() {
        when(outboxService.produceAllUnprocessedEvents()).thenReturn(Map.of());

        sut.poll();

        verify(outboxService).afterProducing(List.of());
    }
}
