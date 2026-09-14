package io.playground.chatservice.common;

import io.playground.chatservice.infrastructure.jpa.entity.UserViewEntity;
import io.playground.chatservice.infrastructure.jpa.repository.UserViewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

@BaseIntegrationTest
public class TestcontainersTest {
  @Autowired
  UserViewRepository userViewRepository;

  @Test
  void save() {
    userViewRepository.save(
        new UserViewEntity(
            1L,
            1L,
            "nickname",
            Instant.now()
        )
    );
  }
}
