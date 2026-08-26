package io.playground.chatservice.infrastructure.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
//@Testcontainers
//@Import(TestContainerConfig.class)
public abstract class BaseIntegrationTest {
}
