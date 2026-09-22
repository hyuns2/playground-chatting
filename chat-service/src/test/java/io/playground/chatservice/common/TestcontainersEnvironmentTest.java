package io.playground.chatservice.common;

import io.playground.chatservice.infrastructure.jpa.entity.UserViewEntity;
import io.playground.chatservice.infrastructure.jpa.repository.UserViewRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@BaseIntegrationTest
public class TestcontainersEnvironmentTest {
  @Autowired
  UserViewRepository userViewRepository;
  @Autowired
  StringRedisTemplate stringRedisTemplate;
  @Autowired
  KafkaTemplate<String, Object> kafkaTemplate;
  @Autowired
  ConsumerFactory<String, Object> consumerFactory;

  @Test
  void mysql() {
    userViewRepository.save(
        new UserViewEntity(
            null, 1L, "me", Instant.now()
        )
    );

    assertThat(userViewRepository.findAllByUserIdIn(List.of(1L)))
        .first()
        .extracting(UserViewEntity::getNickname)
        .isEqualTo("me");
  }

  @Test
  void redis() {
    stringRedisTemplate.opsForValue()
        .set("test-key", "test-value", Duration.ofMinutes(1));

    assertThat(stringRedisTemplate.opsForValue().get("test-key"))
        .isEqualTo("test-value");
  }

  @Test
  void kafka() throws Exception {
    Consumer<String, Object> consumer = consumerFactory.createConsumer(
        "test-consumer-" + UUID.randomUUID()
    );
    consumer.subscribe(List.of("test-topic"));

    kafkaTemplate.send(
        "test-topic", "test-value"
        ).get(5, TimeUnit.SECONDS);

    assertThat(
        KafkaTestUtils.getSingleRecord(
            consumer, "test-topic", Duration.ofSeconds(10)
        ).value()
    ).isEqualTo("test-value");
  }
}
