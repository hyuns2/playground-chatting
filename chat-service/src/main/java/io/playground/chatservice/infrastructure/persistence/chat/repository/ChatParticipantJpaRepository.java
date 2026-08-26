package io.playground.chatservice.infrastructure.persistence.chat.repository;

import io.playground.chatservice.infrastructure.persistence.chat.dto.ChatFlatInfo;
import io.playground.chatservice.infrastructure.persistence.chat.entity.ChatParticipantJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatParticipantJpaRepository extends JpaRepository<ChatParticipantJpaEntity, Long> {
    boolean existsByParticipantIdAndChatRoomId(String participantId, Long chatRoomId);

    @EntityGraph(attributePaths = {"chatRoom", "chatRoom.id"})
    List<ChatParticipantJpaEntity> findAllByParticipantId(String participantId);

//    @Query("SELECT cp FROM ChatParticipantJpaEntity cp " +
//            "JOIN FETCH cp.chatRoom " +
//            "WHERE cp.chatRoom.id IN :chatRoomIds")
//    List<ChatParticipantJpaEntity> findAllWithChatRoomByChatRoomIdIn(List<Long> chatRoomIds);

    @Query("SELECT new io.playground.chatservice.infrastructure.persistence.chat.dto.ChatFlatInfo(" +
                "cr.id, cr.type, cr.name, cr.lastMessagedAt, cp2.participantId, cp2.nickName, cp2.isAdmin) " +
            "FROM ChatParticipantJpaEntity cp " +
            "JOIN cp.chatRoom cr ON cp.chatRoom.id = cr.id " +
            "JOIN ChatParticipantJpaEntity cp2 ON cp2.chatRoom.id = cr.id " +
            "WHERE cp.participantId = :participantId")
    List<ChatFlatInfo> findChatFlatInfosByParticipantId(@Param("participantId") String participantId);

    List<ChatParticipantJpaEntity> findAllByChatRoomId(Long chatRoomId);

    @Modifying
    @Query("UPDATE ChatParticipantJpaEntity cp " +
            "SET cp.lastReadMessageId = :lastReadMessageId " +
            "WHERE cp.participantId = :participantId")
    void updateLastReadMessageIdByParticipantId(Long lastReadMessageId, String participantId);
}
