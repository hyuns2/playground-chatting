package io.playground.chatservice.infrastructure.persistence.chat.adapter;

import io.playground.chatservice.application.chat.dto.ChatDto;
import io.playground.chatservice.application.chat.port.ChatMessageRepositoryPort;
import io.playground.chatservice.domain.chat.message.ChatMessage;
import io.playground.chatservice.infrastructure.persistence.chat.entity.ChatMessageJpaEntity;
import io.playground.chatservice.infrastructure.persistence.chat.entity.ChatRoomJpaEntity;
import io.playground.chatservice.infrastructure.persistence.chat.repository.ChatMessageJpaRepository;
import io.playground.chatservice.infrastructure.persistence.chat.repository.ChatRoomJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatMessagePersistenceAdapter implements ChatMessageRepositoryPort {
    private final ChatMessageJpaRepository chatMessageRepository;
    private final ChatRoomJpaRepository chatRoomRepository;

    @Override
    public boolean existsByIdAndChatRoomId(Long chatMessageId, Long chatRoomId) {
        return chatMessageRepository.existsByIdAndChatRoomId(chatMessageId, chatRoomId);
    }

    @Override
    public Long save(ChatMessage chatMessage) {
        return chatMessageRepository.save(
                    ChatMessageJpaEntity.from(
                            chatMessage,
                            toReferenceChatRoomEntity(chatMessage.getChatRoomId()),
                            chatMessage.getParentMessageId() != null
                                    ? toReferenceChatMessageEntity(chatMessage.getParentMessageId())
                                    : null
                    )
        ).getId();
    }

    private ChatRoomJpaEntity toReferenceChatRoomEntity(Long chatRoomId) {
        return chatRoomRepository.getReferenceById(chatRoomId);
    }

    private ChatMessageJpaEntity toReferenceChatMessageEntity(Long chatMessageId) {
        return chatMessageRepository.getReferenceById(chatMessageId);
    }

    @Override
    public List<ChatDto.ChatMessageInfo> findPageByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable) {
        return chatMessageRepository.findPageByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable).stream()
                .map(ChatMessageJpaEntity::toDto)
                .toList();
    }

    @Override
    public List<ChatDto.ChatMessageInfo> findAllByCursor(Long chatRoomId, LocalDateTime createdAt, Long chatMessageId, int size) {
        return chatMessageRepository.findAllByCursor(
                        chatRoomId,
                        createdAt,
                        chatMessageId,
                        PageRequest.of(0, size)
                ).stream()
                .map(ChatMessageJpaEntity::toDto)
                .toList();
    }
}
