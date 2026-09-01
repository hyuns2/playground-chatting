package io.playground.chatservice.infrastructure.jpa.entity;

import io.playground.chatservice.domain.ChatRoom;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "chat_rooms")
public class ChatRoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatRoom.RoomType type;

    @Column
    private String name;

    @Column
    private Instant lastMessagedAt;

    public static ChatRoomEntity from(ChatRoom chatRoom) {
        return new ChatRoomEntity(
                chatRoom.getId(),
                chatRoom.getType(),
                chatRoom.getName(),
                chatRoom.getLastMessagedAt()
        );
    }

    public ChatRoom toDomain() {
        return ChatRoom.of(
                id,
                type,
                name,
                lastMessagedAt
        );
    }
}
