package io.playground.chatservice.application.chat.port;

import io.playground.chatservice.infrastructure.persistence.chat.dto.ChatFlatInfo;

import java.util.List;
import java.util.Map;

public interface ChatParticipantRepositoryPort {
    boolean existsByChatRoomIdAndParticipantId(Long chatRoomId, String participantId);

    List<ChatFlatInfo> findChatInfosByParticipantId(String participantId);

    Map<String, Long> findLastReadMessageIdInfosByChatRoomId(Long chatRoomId);

    List<Long> findChatRoomIdsByParticipantId(String participantId);

    void updateLastReadMessageIdByParticipantId(Long lastReadMessageId, String participantId);
}
