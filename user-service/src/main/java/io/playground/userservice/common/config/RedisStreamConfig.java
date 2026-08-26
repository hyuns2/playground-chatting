package io.playground.userservice.common.config;

import io.playground.userservice.adapter.messaging.RedisEventConsumer;
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
    private final static String USER_STREAM_NAME = RedisStreamName.USER_STREAM.getValue();
    public final static String USER_GROUP_NAME = "user-group";

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisEventConsumer redisEventConsumer) {
        redisInitConfig.streamInitializer();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(
                        connectionFactory,
                        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                                .pollTimeout(Duration.ofSeconds(2))
                                .build());

        container.receive(
                Consumer.from(USER_GROUP_NAME, "consumer-1"),
                StreamOffset.create(USER_STREAM_NAME, ReadOffset.lastConsumed()),
                redisEventConsumer
        );
        container.receive(
                Consumer.from(USER_GROUP_NAME, "consumer-2"),
                StreamOffset.create(USER_STREAM_NAME, ReadOffset.lastConsumed()),
                redisEventConsumer
        );

        container.start();
        return container;
    }
}
