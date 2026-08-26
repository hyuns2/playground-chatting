package io.playground.userservice.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@RequiredArgsConstructor
public class RedisInitConfig {
    private final StringRedisTemplate stringRedisTemplate;
    private final static String USER_STREAM_NAME = RedisStreamName.USER_STREAM.getValue();
    private final static String USER_GROUP_NAME = RedisStreamConfig.USER_GROUP_NAME;

    public void streamInitializer() {
        try {
            stringRedisTemplate.opsForStream()
                    .createGroup(
                            USER_STREAM_NAME,
                            ReadOffset.latest(),
                            USER_GROUP_NAME
                    );
        } catch (Exception ignored) {
        }
    }
}
