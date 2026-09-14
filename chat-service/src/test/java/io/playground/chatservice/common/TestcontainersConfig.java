package io.playground.chatservice.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {
  @Bean
  @ServiceConnection(name = "mysql")
  MySQLContainer mysqlContainer() {
    return new MySQLContainer("mysql:9.4.0");
  }

  @Bean
  @ServiceConnection(name = "redis")
  @SuppressWarnings("resource")
  GenericContainer<?> redisContainer() {
    return new GenericContainer<>(
        DockerImageName.parse(
            "redis:8.2.1-alpine"
        )
    ).withExposedPorts(6379);
  }

  @Bean
  @ServiceConnection(name = "kafka")
  KafkaContainer kafkaContainer() {
    return new KafkaContainer(
        DockerImageName
            .parse("confluentinc/cp-kafka:8.0.1")
            .asCompatibleSubstituteFor("apache/kafka")
    );
  }
}
