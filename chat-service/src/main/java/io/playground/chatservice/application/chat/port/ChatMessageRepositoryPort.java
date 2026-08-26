package io.playground.chatservice.application.chat.port;

import io.playground.chatservice.application.chat.dto.ChatDto;
import io.playground.chatservice.domain.chat.message.ChatMessage;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepositoryPort {
    boolean existsByIdAndChatRoomId(Long chatMessageId, Long chatRoomId);

    Long save(ChatMessage chatMessage);

    List<ChatDto.ChatMessageInfo> findPageByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);

    List<ChatDto.ChatMessageInfo> findAllByCursor(Long chatRoomId, LocalDateTime createdAt, Long chatMessageId, int size);
}
