package io.playground.chatservice.infrastructure.kafka;

import io.playground.chatservice.exception.BusinessErrorCode;
import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.infrastructure.redis.RedisPubsubConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaChatEventConsumer {
    private final StringRedisTemplate stringRedisTemplate;

    @KafkaListener(
            topics = KafkaEventProducer.TOPIC
    )
    @RetryableTopic
    public void receive(EventEnvelope envelope) {
        switch (envelope.eventType()) {
            case "CHAT_MESSAGE_SENT" -> handleChatMessageSent(
                    envelope.payload()
            );
            default -> throw new BusinessException(
                    BusinessErrorCode.UNKNOWN_EVENT_TYPE,
                    envelope.eventType()
            );
        }
    }

    private void handleChatMessageSent(String payload) {
        stringRedisTemplate.convertAndSend(
                RedisPubsubConfig.CHANNEL,
                payload
        );
    }
}
