package io.playground.chatservice.infrastructure.kafka;

import io.playground.chatservice.exception.BusinessErrorCode;
import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.infrastructure.redis.RedisPubsubConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaChatEventConsumerTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    private KafkaChatEventConsumer sut;

    @BeforeEach
    void setUp() {
        sut = new KafkaChatEventConsumer(stringRedisTemplate);
    }

    @Test
    @DisplayName("CHAT_MESSAGE_SENT 이벤트는 브로드캐스트 채널로 Redis에 발행한다")
    void publishesChatMessageSentToRedis() {
        EventEnvelope envelope = new EventEnvelope(
                "event-1", "CHAT_MESSAGE_SENT", Instant.now(), "trace-1", "room-1", "{\"a\":1}"
        );

        sut.receive(envelope);

        verify(stringRedisTemplate).convertAndSend(RedisPubsubConfig.CHANNEL, "{\"a\":1}");
    }

    @Test
    @DisplayName("알 수 없는 이벤트 타입은 예외를 던진다")
    void throwsForUnknownEventType() {
        EventEnvelope envelope = new EventEnvelope(
                "event-1", "UNKNOWN_TYPE", Instant.now(), "trace-1", "room-1", "{}"
        );

        assertThatThrownBy(() -> sut.receive(envelope))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(BusinessErrorCode.UNKNOWN_EVENT_TYPE);
    }
}
