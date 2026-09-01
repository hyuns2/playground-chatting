package io.playground.chatservice.application.port;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.domain.ChatParticipant;
import io.playground.chatservice.domain.ChatRoom;

import java.util.List;

public interface ChatParticipantPersistencePort {
    boolean existsByChatRoomIdAndParticipantId(Long chatRoomId,
                                               Long participantId);

    List<ChatRoom> findChatRoomsByParticipantId(Long participantId,
                                                int page,
                                                int size);

    List<ChatParticipant> findAllByChatRoomIds(List<Long> chatRoomIds);

    List<ChatQueryDto.LastReadMessageInfo> findLastReadMessageInfosByChatRoomId(Long chatRoomId);

    void saveAll(List<ChatParticipant> chatParticipants);

    void updateLastReadMessageIdById(Long id,
                                     Long lastReadMessageId);
}
