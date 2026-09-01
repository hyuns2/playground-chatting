package io.playground.chatservice.infrastructure.jpa.repository;

import io.playground.chatservice.infrastructure.jpa.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
    @Modifying
    @Query("""
        update ChatRoomEntity e
            set e.lastMessagedAt = :lastMessagedAt
        where e.id = :chatRoomId
    """)
    void updateLastMessagedAtByChatRoomId(Long chatRoomId,
                                          Instant lastMessagedAt);
}
