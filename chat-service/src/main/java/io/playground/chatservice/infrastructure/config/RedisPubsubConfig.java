package io.playground.chatservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisPubsubConfig {
    @Bean
    public RedisMessageListenerContainer messageListenerContainer(RedisConnectionFactory connectionFactory,
                                                   MessageListener messageListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(messageListener, new PatternTopic("/broadcast/chat"));

        return container;
    }
}
