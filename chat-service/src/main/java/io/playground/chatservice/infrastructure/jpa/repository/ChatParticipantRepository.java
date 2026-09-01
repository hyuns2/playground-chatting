package io.playground.chatservice.infrastructure.jpa.repository;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.infrastructure.jpa.entity.ChatParticipantEntity;
import io.playground.chatservice.infrastructure.jpa.entity.ChatRoomEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipantEntity, Long> {
    boolean existsByChatRoomIdAndParticipantId(Long chatRoomId,
                                               Long participantId);

    @Query("""
        select e.chatRoom from ChatParticipantEntity e
        where e.participantId = :participantId
    """)
    Page<ChatRoomEntity> findChatRoomsByParticipantId(Long participantId,
                                                      Pageable pageable);

    List<ChatParticipantEntity> findAllByChatRoom_IdIn(List<Long> chatRoomIds);

    @Query("""
        select new io.playground.chatservice.application.dto.ChatQueryDto$LastReadMessageInfo(
            e.id,
            e.participantId,
            e.lastReadMessageId
        ) from ChatParticipantEntity e
        where e.chatRoom.id = :chatRoomId
    """)
    List<ChatQueryDto.LastReadMessageInfo> findLastReadMessageInfosByChatRoomId(Long chatRoomId);

    @Modifying
    @Query("""
        update ChatParticipantEntity e
            set e.lastReadMessageId = :lastReadMessageId
        where e.id = :id
    """)
    void updateLastReadMessageIdById(Long id,
                                     Long lastReadMessageId);
}
