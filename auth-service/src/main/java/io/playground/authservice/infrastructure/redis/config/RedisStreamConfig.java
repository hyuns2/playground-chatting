package io.playground.authservice.infrastructure.redis.config;

import io.playground.authservice.infrastructure.redis.messaging.cosumer.EventConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisStreamConfig {
    private final RedisInitConfig redisInitConfig;
    private final static String AUTH_STREAM_NAME = RedisStreamName.AUTH_STREAM.getValue();
    public final static String AUTH_GROUP_NAME = "auth-group";

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamListenerContainer(
            RedisConnectionFactory connectionFactory,
            EventConsumer eventConsumer) {
        redisInitConfig.streamInitializer();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(
                        connectionFactory,
                        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                                .pollTimeout(Duration.ofSeconds(2))
                                .build()
                );

        container.receive(
                Consumer.from(AUTH_GROUP_NAME, "consumer-1"),
                StreamOffset.create(AUTH_STREAM_NAME, ReadOffset.lastConsumed()),
                eventConsumer
        );
        container.receive(
                Consumer.from(AUTH_GROUP_NAME, "consumer-2"),
                StreamOffset.create(AUTH_STREAM_NAME, ReadOffset.lastConsumed()),
                eventConsumer
        );

        container.start();
        return container;
    }
}
