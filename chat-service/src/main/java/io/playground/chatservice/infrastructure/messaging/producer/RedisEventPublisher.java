package io.playground.chatservice.infrastructure.messaging.producer;

import io.playground.chatservice.exception.CustomErrorCode;
import io.playground.chatservice.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisEventPublisher {
    private final StringRedisTemplate stringRedisTemplate;

    public void publish(String streamName, Map<String, String> envelope) {
        try {
            stringRedisTemplate.opsForStream()
                    .add(
                            StreamRecords
                                    .mapBacked(envelope)
                                    .withStreamKey(streamName)
                    );
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.PRODUCING_FAILED);
        }
    }
}
