package io.playground.authservice.infrastructure.redis.messaging.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.authservice.domain.event.AuthSignUpEvent;
import io.playground.authservice.exception.CustomErrorCode;
import io.playground.authservice.exception.CustomException;
import io.playground.authservice.infrastructure.redis.config.RedisStreamName;
import io.playground.authservice.infrastructure.redis.messaging.constant.EventEnvelope;
import io.playground.authservice.infrastructure.redis.messaging.constant.PubEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EventPublisher {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(Object event) {
        if (AuthSignUpEvent.class.equals(event.getClass()))
            publish(RedisStreamName.USER_STREAM.getValue(),
                    PubEventType.AUTH_SIGN_UP_EVENT.getValue(),
                    event);
        else
            throw new CustomException(CustomErrorCode.UNKNOWN_EVENT_TYPE);
    }

    private void publish(String streamName, String eventType, Object event) {
        try {
            stringRedisTemplate.opsForStream()
                    .add(StreamRecords
                            .mapBacked(EventEnvelope
                                    .of(eventType,
                                            objectMapper.writeValueAsString(event))
                                    .toMap()
                            )
                            .withStreamKey(streamName)
                    );
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.PUBLISHING_FAILED);
        }
    }
}
