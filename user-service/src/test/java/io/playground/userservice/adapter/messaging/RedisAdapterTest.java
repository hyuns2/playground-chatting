package io.playground.userservice.adapter.messaging;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Import(RedisAdapterTestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RedisAdapterTest {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TestPublisher testPublisher;
    @Autowired
    private TestConsumer testConsumer;
    private static final String USER_STREAM_NAME = "user-events";
    private static final String USER_GROUP_NAME = "user-group";
    private static final String CONSUMER_1_NAME = "test-consumer-1";
    private static final String CONSUMER_2_NAME = "test-consumer-2";

    @BeforeAll
    void before() {
        try {
            stringRedisTemplate.delete(USER_STREAM_NAME);

            stringRedisTemplate.opsForStream()
                    .createGroup(
                            USER_STREAM_NAME,
                            ReadOffset.from("0"),
                            USER_GROUP_NAME
                    );
        } catch (Exception e) {
        }
    }

    @Test
    void test_publish_and_consume() throws InterruptedException {
        // given
        testPublisher.publish("test-payload-1");
        testPublisher.publish("test-payload-2");
        testPublisher.publish("test-payload-3");

        List<String> result = new ArrayList<>();
        Thread.sleep(2000);

        // when
        List<MapRecord<String, String, String>> messages = testConsumer.read(CONSUMER_1_NAME);
        for (MapRecord<String, String, String> message : messages) {
            if (message.getValue().get("payload").equals("test-payload-1"))
                continue;

            log.info("Consumed message id [{}], payload [{}] \n from stream [{}]",
                    message.getId(),
                    message.getValue().get("payload"),
                    USER_STREAM_NAME);

            result.add(message.getValue().get("payload"));

            testConsumer.ack(message);
        }

        // then
        Assertions.assertThat(result).containsOnly("test-payload-2", "test-payload-3");
        Assertions.assertThat(result.size()).isEqualTo(2);
    }

    // Todo: pending 후 claim까지 정상 / 이후 read 시점 알 수 없는 문제
    @Test
    void test_pending_claim() throws InterruptedException {
        // given
        testPublisher.publish("test-payload-1");
        testPublisher.publish("test-payload-2");
        testPublisher.publish("test-payload-3");

        Thread.sleep(2000);

        List<MapRecord<String, String, String>> messages = testConsumer.read(CONSUMER_1_NAME);
        for (MapRecord<String, String, String> message : messages) {
            if (!message.getValue().get("payload").equals("test-payload-1"))
                continue;

            log.info("Consumed message id [{}], payload [{}] \n from stream [{}]",
                    message.getId(),
                    message.getValue().get("payload"),
                    USER_STREAM_NAME);

            testConsumer.ack(message);
        }

        // when
        List<MapRecord<String, String, String>> pendingRecords =
                testConsumer.claimPendingMessages(
                        CONSUMER_2_NAME,
                        testConsumer.getPendingMessages()
                );
        for (MapRecord<String, String, String> record : pendingRecords)
            log.info("Claimed pending message id [{}], payload [{}] \n from stream [{}]",
                    record.getId().getValue(),
                    record.getValue().get("payload"),
                    USER_STREAM_NAME);

        List<MapRecord<String, String, String>> resultRecords = new ArrayList<>();
        while (resultRecords.isEmpty())
            resultRecords = testConsumer.read(CONSUMER_2_NAME);

        List<String> resultPayloads = resultRecords.stream()
                .map(m -> m.getValue().get("payload"))
                .toList();
        log.info("result size: {}", resultPayloads.size());

        // then
        Assertions.assertThat(resultPayloads.size()).isEqualTo(2);
        Assertions.assertThat(resultPayloads).containsOnly("test-payload-2", "test-payload-3");
        Assertions.assertThat(testConsumer.getPendingMessages().size()).isEqualTo(0);
    }
}
