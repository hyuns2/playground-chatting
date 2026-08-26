package io.playground.authservice.infrastructure.redis.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@RequiredArgsConstructor
public class RedisInitConfig {
    private final StringRedisTemplate stringRedisTemplate;
    private final static String AUTH_STREAM_NAME = RedisStreamName.AUTH_STREAM.getValue();
    private final static String AUTH_GROUP_NAME = RedisStreamConfig.AUTH_GROUP_NAME;

    public void streamInitializer() {
        try {
            stringRedisTemplate.opsForStream()
                    .createGroup(
                            AUTH_STREAM_NAME,
                            ReadOffset.latest(),
                            AUTH_GROUP_NAME
                    );
        } catch (Exception ignored) {
        }
    }
}
