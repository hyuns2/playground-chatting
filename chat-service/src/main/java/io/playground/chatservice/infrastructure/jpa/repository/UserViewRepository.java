package io.playground.chatservice.infrastructure.jpa.repository;

import io.playground.chatservice.infrastructure.jpa.entity.UserViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserViewRepository extends JpaRepository<UserViewEntity, Long> {
    List<UserViewEntity> findAllByUserIdIn(List<Long> userIds);
}
