package io.playground.chatservice.infrastructure.jpa.entity;

import io.playground.chatservice.domain.ChatParticipant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "chat_participants")
public class ChatParticipantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ChatRoomEntity chatRoom;

    @Column(nullable = false)
    private Long participantId;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private boolean isAdmin;

    @Column
    private Long lastReadMessageId;

    public static ChatParticipantEntity from(ChatParticipant chatParticipant,
                                             ChatRoomEntity chatRoom) {
        return new ChatParticipantEntity(
                chatParticipant.getId(),
                chatRoom,
                chatParticipant.getParticipantId(),
                chatParticipant.getNickname(),
                chatParticipant.isAdmin(),
                chatParticipant.getLastReadMessageId()
        );
    }

    public ChatParticipant toDomain() {
        return ChatParticipant.of(
                id,
                chatRoom.getId(),
                participantId,
                nickname,
                isAdmin,
                lastReadMessageId
        );
    }
}
