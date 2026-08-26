package io.playground.chatservice.infrastructure.persistence.chat.repository;

import io.playground.chatservice.infrastructure.persistence.chat.entity.ChatRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoomJpaEntity, Long> {
    @Modifying
    @Query("update ChatRoomJpaEntity c set c.lastMessagedAt = :createdAt where c.id = :chatRoomId")
    void updateLastMessageAtByChatRoomId(LocalDateTime createdAt, Long chatRoomId);
}
