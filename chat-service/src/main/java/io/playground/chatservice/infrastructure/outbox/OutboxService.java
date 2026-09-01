package io.playground.chatservice.infrastructure.outbox;

import io.playground.chatservice.infrastructure.kafka.EventEnvelope;
import io.playground.chatservice.infrastructure.kafka.KafkaEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final KafkaEventProducer kafkaEventProducer;

    @Transactional(readOnly = true)
    public Map<Long, CompletableFuture<?>> produceAllUnprocessedEvents() {
        return outboxRepository
                .findAllByProcessedOrderByOccurredAtAsc(false).stream()
                .collect(
                        Collectors.toMap(
                                OutboxEntity::getId,
                                this::produceEvent
                        )
                );
    }

    private CompletableFuture<SendResult<String, Object>> produceEvent(OutboxEntity event) {
        return kafkaEventProducer.send(
                EventEnvelope.from(event)
        );
    }

    @Transactional
    public void afterProducing(List<Long> successIds) {
        if (!successIds.isEmpty())
            outboxRepository.updateProcessedTrueByIds(
                    successIds
            );
    }
}
