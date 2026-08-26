package io.playground.chatservice.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.chatservice.application.chat.handler.MessageHandler;
import io.playground.chatservice.exception.CustomErrorCode;
import io.playground.chatservice.exception.CustomException;
import io.playground.chatservice.infrastructure.messaging.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RedisEventStreamListenerManager {
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    public <T> void subscribe(String streamName,
                             String groupName,
                             String consumerName,
                             Class<T> eventClassType,
                             MessageHandler<T> messageHandler) {
        String key = getSubscriptionKey(streamName, groupName, consumerName);

        if (subscriptions.containsKey(key))
            return;

        ensureGroup(streamName, groupName);

        Subscription subscription = container.receive(
                Consumer.from(groupName, consumerName),
                StreamOffset.create(streamName, ReadOffset.lastConsumed()),
                message -> {
                    try {
                        EventEnvelope eventEnvelope = objectMapper.convertValue(
                                message.getValue(),
                                EventEnvelope.class
                        );

                        messageHandler.handle(
                                objectMapper.readValue(
                                        eventEnvelope.getPayload(),
                                        eventClassType
                                )
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CustomException(CustomErrorCode.CONSUMING_FAILED);
                    } finally {
                        ack(streamName, groupName, message.getId().getValue());
                    }
                }
        );

        subscriptions.put(key, subscription);
    }

    private void ensureGroup(String streamName, String groupName) {
        try {
            stringRedisTemplate.opsForStream()
                    .createGroup(
                            streamName,
                            ReadOffset.latest(),
                            groupName
                    );
        } catch (Exception ignored) {
        }
    }

    private void ack(String streamName, String groupName, String messageId) {
        stringRedisTemplate.opsForStream()
                .acknowledge(
                        streamName,
                        groupName,
                        messageId
                );
    }

    public void unsubscribe(String streamName, String groupName, String consumerName) {
        String key = getSubscriptionKey(streamName, groupName, consumerName);

        if (!subscriptions.containsKey(key))
            return;

        Subscription subscription = subscriptions.remove(key);
        if (subscription != null)
            subscription.cancel();
    }

    private String getSubscriptionKey(String streamName, String groupName, String consumerName) {
        return streamName + ":" + groupName + ":" + consumerName;
    }
}
