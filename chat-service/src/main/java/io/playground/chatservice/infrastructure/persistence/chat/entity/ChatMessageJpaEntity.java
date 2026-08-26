package io.playground.chatservice.infrastructure.persistence.chat.entity;

import io.playground.chatservice.application.chat.dto.ChatDto;
import io.playground.chatservice.domain.chat.message.ChatMessage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "CHAT_MESSAGE", indexes = {
        @Index(name = "idx_chatRoomId_createdAt", columnList = "chat_room_id, created_at"),
        @Index(name = "idx_createdAt_id", columnList = "created_at, id")
})
public class ChatMessageJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoomJpaEntity chatRoom;

    @Column(nullable = false)
    private String senderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatMessage.MessageType type;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private ChatMessageJpaEntity parentMessage;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public static ChatMessageJpaEntity of(Long id,
                                          ChatRoomJpaEntity chatRoom,
                                          String senderId,
                                          ChatMessage.MessageType type,
                                          String content,
                                          ChatMessageJpaEntity parentMessage,
                                          LocalDateTime createdAt) {
        return new ChatMessageJpaEntity(
                id,
                chatRoom,
                senderId,
                type,
                content,
                parentMessage,
                createdAt
        );
    }

    public static ChatMessageJpaEntity from(ChatMessage chatMessage, ChatRoomJpaEntity chatRoom, ChatMessageJpaEntity parentMessage) {
        return new ChatMessageJpaEntity(
                chatMessage.getId(),
                chatRoom,
                chatMessage.getSenderId(),
                chatMessage.getType(),
                chatMessage.getContent(),
                parentMessage,
                chatMessage.getCreatedAt()
        );
    }

    public static ChatDto.ChatMessageInfo toDto(ChatMessageJpaEntity entity) {
        return new ChatDto.ChatMessageInfo(
                entity.getId(),
                entity.getChatRoom().getId(),
                entity.getSenderId(),
                entity.getType(),
                entity.getContent(),
                entity.getParentMessage() != null ? entity.getParentMessage().getId() : null,
                entity.getCreatedAt()
        );
    }
}
