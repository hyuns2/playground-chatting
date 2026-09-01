package io.playground.chatservice.application.port;

import io.playground.chatservice.domain.ChatRoom;

import java.time.Instant;
import java.util.List;

public interface ChatRoomPersistencePort {
    ChatRoom save(ChatRoom chatRoom);

    void updateLastMessagedAtByChatRoomId(Long chatRoomId,
                                          Instant lastMessagedAt);
}
