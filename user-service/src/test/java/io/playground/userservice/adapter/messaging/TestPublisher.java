package io.playground.userservice.adapter.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

@Slf4j
public class TestPublisher {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private final static String USER_STREAM_NAME = "user-events";

    private Map<String, String> createEnvelope(String payload) {
        return Map.of("payload", payload);
    }

    public void publish(String payload) {
        stringRedisTemplate.opsForStream()
                .add(USER_STREAM_NAME, createEnvelope(payload));

        log.info("Published message: payload [{}]",
                payload);
    }
}
