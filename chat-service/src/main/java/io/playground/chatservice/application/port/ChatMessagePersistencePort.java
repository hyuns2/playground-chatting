package io.playground.chatservice.application.port;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.domain.ChatMessage;

import java.util.List;

public interface ChatMessagePersistencePort {
    boolean existsByIdAndChatRoomId(Long id,
                                    Long chatRoomId);

    List<ChatMessage> findAllByChatRoomIdAndCursor(Long chatRoomId,
                                                   int size,
                                                   ChatQueryDto.ChatMessageCursor cursor);

    ChatMessage save(ChatMessage chatMessage);
}
