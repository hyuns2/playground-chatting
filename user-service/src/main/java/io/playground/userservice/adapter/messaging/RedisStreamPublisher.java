package io.playground.userservice.adapter.messaging;

import io.playground.userservice.adapter.outbox.EventLogEntity;
import io.playground.userservice.common.exception.CustomErrorCode;
import io.playground.userservice.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisStreamPublisher {
    private final StringRedisTemplate stringRedisTemplate;

    public void publish(String StreamName, EventLogEntity eventLog) {
        try {
            stringRedisTemplate.opsForStream()
                    .add(StreamRecords
                            .mapBacked(
                                    EventEnvelope.builder()
                                            .eventId(eventLog.getEventId())
                                            .eventType(eventLog.getEventType().getValue())
                                            .payload(eventLog.getPayload())
                                            .build().toMap()
                            )
                            .withStreamKey(StreamName)
                    );
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.PUBLISHING_FAILED);
        }
    }
}
