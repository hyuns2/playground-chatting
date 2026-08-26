package io.playground.chatservice.application.chat.port;

import io.playground.chatservice.domain.chat.ChatParticipant;
import io.playground.chatservice.domain.chat.room.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRoomRepositoryPort {
    Long saveChatRoomWithParticipants(ChatRoom chatRoom, List<ChatParticipant> chatParticipants);

    void updateLastMessageAtByChatRoomId(LocalDateTime createdAt, Long chatRoomId);
}
