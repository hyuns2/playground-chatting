package io.playground.chatservice.infrastructure.persistence.chat.entity;

import io.playground.chatservice.domain.chat.ChatParticipant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "CHAT_PARTICIPANT", indexes = {
        @Index(name = "idx_participantId_chatRoomId", columnList = "participant_id, chat_room_id")
})
public class ChatParticipantJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoomJpaEntity chatRoom;

    @Column(nullable = false)
    private String participantId;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private boolean isAdmin;

    @Column
    private Long lastReadMessageId;

    public static ChatParticipantJpaEntity of(Long id,
                                              ChatRoomJpaEntity chatRoomJpaEntity,
                                              String participantId,
                                              String nickName,
                                              boolean isAdmin,
                                              Long lastReadMessageId) {
        return new ChatParticipantJpaEntity(
                id,
                chatRoomJpaEntity,
                participantId,
                nickName,
                isAdmin,
                lastReadMessageId
        );
    }

    public static ChatParticipantJpaEntity from(ChatParticipant chatParticipant,
                                                ChatRoomJpaEntity chatRoomEntity) {
        return new ChatParticipantJpaEntity(
                chatParticipant.getId(),
                chatRoomEntity,
                chatParticipant.getParticipantId(),
                chatParticipant.getNickName(),
                chatParticipant.isAdmin(),
                chatParticipant.getLastReadMessageId()
        );
    }
}
