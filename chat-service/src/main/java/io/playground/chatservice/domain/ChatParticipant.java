package io.playground.chatservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatParticipant {
    private Long id;

    private Long chatRoomId;

    private Long participantId;

    private String nickname;

    private boolean isAdmin;

    private Long lastReadMessageId;

    public static ChatParticipant of(Long id,
                                     Long chatRoomId,
                                     Long participantId,
                                     String nickname,
                                     boolean isAdmin,
                                     Long lastReadMessageId) {
        return new ChatParticipant(
                id,
                chatRoomId,
                participantId,
                nickname,
                isAdmin,
                lastReadMessageId);
    }
}
