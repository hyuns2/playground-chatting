package io.playground.chatservice.infrastructure.jpa.adapter;

import io.playground.chatservice.application.port.ChatRoomPersistencePort;
import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.infrastructure.jpa.entity.ChatRoomEntity;
import io.playground.chatservice.infrastructure.jpa.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ChatRoomPersistenceAdapter implements ChatRoomPersistencePort {
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        return chatRoomRepository
                .save(
                        ChatRoomEntity.from(chatRoom)
                )
                .toDomain();
    }

    @Override
    public void updateLastMessagedAtByChatRoomId(Long chatRoomId,
                                                 Instant lastMessagedAt) {
        chatRoomRepository
                .updateLastMessagedAtByChatRoomId(
                        chatRoomId,
                        lastMessagedAt
                );
    }
}
