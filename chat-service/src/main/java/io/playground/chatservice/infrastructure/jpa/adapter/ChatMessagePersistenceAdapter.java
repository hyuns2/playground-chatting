package io.playground.chatservice.infrastructure.jpa.adapter;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.application.port.ChatMessagePersistencePort;
import io.playground.chatservice.domain.ChatMessage;
import io.playground.chatservice.infrastructure.jpa.entity.ChatMessageEntity;
import io.playground.chatservice.infrastructure.jpa.repository.ChatMessageRepository;
import io.playground.chatservice.infrastructure.jpa.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatMessagePersistenceAdapter implements ChatMessagePersistencePort {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public boolean existsByIdAndChatRoomId(Long id, Long chatRoomId) {
        return chatMessageRepository
                .existsByIdAndChatRoomId(id, chatRoomId);
    }

    @Override
    public List<ChatMessage> findAllByChatRoomIdAndCursor(Long chatRoomId,
                                                          int size,
                                                          ChatQueryDto.ChatMessageCursor cursor) {
        return chatMessageRepository
                .findAllByChatRoomIdAndIdAndCreatedAt(
                        chatRoomId,
                        cursor.id(),
                        cursor.createdAt(),
                        size
                ).stream()
                .map(ChatMessageEntity::toDomain)
                .toList();
    }

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(
                ChatMessageEntity.from(
                        chatMessage,
                        chatRoomRepository.getReferenceById(
                                chatMessage.getChatRoomId()
                        ),
                        chatMessage.getParentMessageId() != null ?
                                chatMessageRepository.getReferenceById(
                                        chatMessage.getParentMessageId()
                                ) :
                                null
                )
        ).toDomain();
    }
}
