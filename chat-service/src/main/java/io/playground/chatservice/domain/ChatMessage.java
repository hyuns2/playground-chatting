package io.playground.chatservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ChatMessage {
    private Long id;

    private Long chatRoomId;

    private Long senderId;

    private MessageType type;

    private String content;

    private Long parentMessageId;

    private Instant createdAt;

    public enum MessageType {
        IN, OUT, TEXT
    }

    public static ChatMessage of(Long id,
                                 Long chatRoomId,
                                 Long senderId,
                                 MessageType type,
                                 String content,
                                 Long parentMessageId,
                                 Instant createdAt) {
        return new ChatMessage(
                id,
                chatRoomId,
                senderId,
                type,
                content,
                parentMessageId,
                createdAt
        );
    }
}
