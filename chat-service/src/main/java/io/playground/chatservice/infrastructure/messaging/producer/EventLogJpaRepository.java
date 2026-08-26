package io.playground.chatservice.infrastructure.messaging.producer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventLogJpaRepository extends JpaRepository<EventLogJpaEntity, String> {
    List<EventLogJpaEntity> findAllByProcessedFalse();
}
