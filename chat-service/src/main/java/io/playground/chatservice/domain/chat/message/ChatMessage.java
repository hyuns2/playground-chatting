package io.playground.chatservice.domain.chat.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessage {
    private Long id;

    private Long chatRoomId;

    private String senderId;

    private MessageType type;

    private String content;

    private Long parentMessageId;

    private LocalDateTime createdAt;

    public enum MessageType {
        IN, OUT, TEXT, IMAGE, FILE
    }

    public static ChatMessage of(Long id,
                                 Long chatRoomId,
                                 String senderId,
                                 MessageType type,
                                 String content,
                                 Long parentMessageId,
                                 LocalDateTime createdAt) {
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
