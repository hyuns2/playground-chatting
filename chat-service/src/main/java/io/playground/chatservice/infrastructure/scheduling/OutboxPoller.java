package io.playground.chatservice.infrastructure.scheduling;

import io.playground.chatservice.infrastructure.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class OutboxPoller {
    private final OutboxService outboxService;

    @Scheduled(fixedDelay = 10000)
    public void poll() {
        Map<Long, CompletableFuture<?>> results =
                outboxService.produceAllUnprocessedEvents();

        CompletableFuture.allOf(
                results.values()
                        .toArray(CompletableFuture[]::new)
        ).whenComplete((ignored, ex) -> {
            List<Long> successIds = results.entrySet().stream()
                    .filter(entry ->
                        !entry.getValue().isCompletedExceptionally()
                    )
                    .map(Map.Entry::getKey)
                    .toList();

            outboxService.afterProducing(successIds);
        });
    }
}
