package io.playground.chatservice.infrastructure.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaEventProducer sut;

    @BeforeEach
    void setUp() {
        sut = new KafkaEventProducer(kafkaTemplate);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("고정된 토픽과 partitionKey로 KafkaTemplate에 발행을 위임한다")
    void sendsEnvelopeToFixedTopic() {
        EventEnvelope envelope = new EventEnvelope(
                "event-1", "CHAT_MESSAGE_SENT", Instant.now(), "trace-1", "room-1", "{}"
        );
        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.completedFuture(org.mockito.Mockito.mock(SendResult.class));
        when(kafkaTemplate.send(KafkaEventProducer.TOPIC, "room-1", envelope)).thenReturn(future);

        CompletableFuture<SendResult<String, Object>> result = sut.send(envelope);

        assertThat(result).isSameAs(future);
    }
}
