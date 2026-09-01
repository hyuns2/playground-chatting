package io.playground.chatservice.infrastructure.jpa.entity;

import io.playground.chatservice.domain.ChatMessage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "chat_messages")
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ChatRoomEntity chatRoom;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatMessage.MessageType type;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatMessageEntity parentMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static ChatMessageEntity from(ChatMessage chatMessage,
                                         ChatRoomEntity chatRoom,
                                         ChatMessageEntity parentMessage) {
        return new ChatMessageEntity(
                chatMessage.getId(),
                chatRoom,
                chatMessage.getSenderId(),
                chatMessage.getType(),
                chatMessage.getContent(),
                parentMessage,
                chatMessage.getCreatedAt()
        );
    }

    public ChatMessage toDomain() {
        return ChatMessage.of(
                id,
                chatRoom.getId(),
                senderId,
                type,
                content,
                parentMessage != null ?
                        parentMessage.getId() :
                        null,
                createdAt
        );
    }
}
