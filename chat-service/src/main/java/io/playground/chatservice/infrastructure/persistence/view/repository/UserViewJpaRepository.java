package io.playground.chatservice.infrastructure.persistence.view.repository;

import io.playground.chatservice.infrastructure.persistence.view.entity.UserViewJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserViewJpaRepository extends JpaRepository<UserViewJpaEntity, String> {
}
