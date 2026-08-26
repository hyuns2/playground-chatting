package io.playground.userservice.adapter.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;

@Slf4j
public class TestConsumer {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final String USER_STREAM_NAME = "user-events";
    private static final String USER_GROUP_NAME = "user-group";

    public List<MapRecord<String, String, String>> read(String consumerName) {
        return stringRedisTemplate.<String, String>opsForStream()
                .read(
                        Consumer.from(USER_GROUP_NAME, consumerName),
                        StreamReadOptions.empty().count(10).block(Duration.ofSeconds(1)),
                        StreamOffset.create(USER_STREAM_NAME, ReadOffset.lastConsumed())
                );
    }

    public void ack(MapRecord<String, String, String> message) {
        stringRedisTemplate.opsForStream()
                .acknowledge(
                        USER_STREAM_NAME,
                        USER_GROUP_NAME,
                        message.getId()
                );
    }

    public List<PendingMessage> getPendingMessages() {
        return stringRedisTemplate.opsForStream()
                .pending(
                        USER_STREAM_NAME,
                        USER_GROUP_NAME,
                        Range.unbounded(), 20
                ).get().toList();
    }

    public List<MapRecord<String, String, String>> claimPendingMessages(String consumerName, List<PendingMessage> pendingMessages) {
        List<RecordId> recordIds = pendingMessages.stream()
                .map(PendingMessage::getId)
                .toList();

        return stringRedisTemplate.<String, String>opsForStream()
                .claim(
                        USER_STREAM_NAME,
                        USER_GROUP_NAME,
                        consumerName,
                        Duration.ofNanos(1L),
                        recordIds.toArray(new RecordId[0])
                );
    }
}
