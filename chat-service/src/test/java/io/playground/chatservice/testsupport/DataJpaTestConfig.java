package io.playground.chatservice.testsupport;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * {@code ChatServiceApplication} carries {@code @EnableWebSocketMessageBroker} directly
 * (in addition to {@code WebsocketConfig}), which {@code @DataJpaTest} would otherwise
 * pick up as its root configuration and fail to start (STOMP handler wiring lives in
 * {@code WebsocketConfig}, which a JPA slice test never scans). {@code @DataJpaTest}
 * classes reference this minimal config instead via {@code @ContextConfiguration}.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@AutoConfigurationPackage(basePackages = "io.playground.chatservice")
public class DataJpaTestConfig {
}
