package io.playground.chatservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KafkaEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String TOPIC = "order.events";

    public CompletableFuture<SendResult<String, Object>> send(EventEnvelope envelope) {
        return kafkaTemplate.send(
                TOPIC,
                envelope.partitionKey(),
                envelope
        );
    }
}
