package io.playground.chatservice.infrastructure.persistence.chat.adapter;

import io.playground.chatservice.application.chat.port.ChatRoomRepositoryPort;
import io.playground.chatservice.domain.chat.ChatParticipant;
import io.playground.chatservice.domain.chat.room.ChatRoom;
import io.playground.chatservice.infrastructure.persistence.chat.entity.ChatParticipantJpaEntity;
import io.playground.chatservice.infrastructure.persistence.chat.entity.ChatRoomJpaEntity;
import io.playground.chatservice.infrastructure.persistence.chat.repository.ChatParticipantJpaRepository;
import io.playground.chatservice.infrastructure.persistence.chat.repository.ChatRoomJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatRoomPersistenceAdapter implements ChatRoomRepositoryPort {
    private final ChatRoomJpaRepository chatRoomRepository;
    private final ChatParticipantJpaRepository chatParticipantRepository;

    @Override
    public Long saveChatRoomWithParticipants(ChatRoom chatRoom, List<ChatParticipant> chatParticipants) {
        ChatRoomJpaEntity chatRoomEntity = chatRoomRepository.save(
                ChatRoomJpaEntity.from(chatRoom)
        );

        chatParticipantRepository.saveAll(
                chatParticipants.stream()
                        .map(chatParticipant ->
                                ChatParticipantJpaEntity.from(
                                        chatParticipant,
                                        chatRoomEntity
                                )
                        ).toList()
        );

        return chatRoomEntity.getId();
    }

    @Override
    public void updateLastMessageAtByChatRoomId(LocalDateTime createdAt, Long chatRoomId) {
        chatRoomRepository.updateLastMessageAtByChatRoomId(
                createdAt, chatRoomId);
    }
}
