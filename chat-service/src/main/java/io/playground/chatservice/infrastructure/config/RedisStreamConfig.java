package io.playground.chatservice.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisStreamConfig {
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamListenerContainer(RedisConnectionFactory connectionFactory) {
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(
                        connectionFactory,
                        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                                .pollTimeout(Duration.ofSeconds(2))
                                .build()
                );

        container.start();
        return container;
    }
}
