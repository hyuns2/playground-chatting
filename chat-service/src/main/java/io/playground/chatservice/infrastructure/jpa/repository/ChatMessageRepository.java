package io.playground.chatservice.infrastructure.jpa.repository;

import io.playground.chatservice.infrastructure.jpa.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    boolean existsByIdAndChatRoomId(Long id, Long chatRoomId);

    @Query("""
        select e from ChatMessageEntity e
        where e.chatRoom.id = :chatRoomId
            and (
                :id is null
                    or :createdAt is null
                    or (
                        e.createdAt < :createdAt
                        or (e.createdAt = :createdAt and e.id < :id)
                    )
                )
        order by e.createdAt desc, e.id desc
        limit :size
    """)
    List<ChatMessageEntity> findAllByChatRoomIdAndIdAndCreatedAt(Long chatRoomId,
                                                                 Long id,
                                                                 Instant createdAt,
                                                                 int size);


}
