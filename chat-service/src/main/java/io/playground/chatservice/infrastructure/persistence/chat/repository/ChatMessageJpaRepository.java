package io.playground.chatservice.infrastructure.persistence.chat.repository;

import io.playground.chatservice.infrastructure.persistence.chat.entity.ChatMessageJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageJpaEntity, Long> {
    boolean existsByIdAndChatRoomId(Long chatMessageId, Long chatRoomId);

    List<ChatMessageJpaEntity> findPageByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);

    @Query("select cm from ChatMessageJpaEntity cm " +
            "where cm.chatRoom.id = :chatRoomId " +
            "and (" +
                "cm.createdAt < :createdAt " +
                "or (cm.createdAt = :createdAt " +
                    " and (:chatMessageId is null or cm.id < :chatMessageId))" +
            ") " +
            "order by cm.createdAt desc, cm.id desc "
    )
    List<ChatMessageJpaEntity> findAllByCursor(@Param("chatRoomId") Long chatRoomId,
                                               @Param("createdAt") LocalDateTime createdAt,
                                               @Param("chatMessageId") Long chatMessageId,
                                               Pageable pageable);
}
