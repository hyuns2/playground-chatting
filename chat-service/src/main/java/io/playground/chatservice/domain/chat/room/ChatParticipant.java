package io.playground.chatservice.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatParticipant {
    private Long id;

    private Long chatRoomId;

    private String participantId;

    private String nickName;

    private boolean isAdmin;

    private Long lastReadMessageId;

    public static ChatParticipant of(Long id,
                                     Long chatRoomId,
                                     String participantId,
                                     String nickName,
                                     boolean isAdmin,
                                     Long lastReadMessageId) {
        return new ChatParticipant(
                id,
                chatRoomId,
                participantId,
                nickName,
                isAdmin,
                lastReadMessageId);
    }
}
