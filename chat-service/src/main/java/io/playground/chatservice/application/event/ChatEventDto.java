package io.playground.chatservice.application.event;

import io.playground.chatservice.domain.ChatMessage;

import java.time.Instant;

public class ChatEventDto {
    public enum EventType {
        CHAT_MESSAGE_SENT
    }

    public record ChatMessageSent(
            Long chatMessageId,
            Long chatRoomId,
            Long senderId,
            ChatMessage.MessageType type,
            String content,
            Long parentMessageId,
            Instant createdAt
    ) {
        public static ChatMessageSent from(ChatMessage chatMessage) {
            return new ChatMessageSent(
                    chatMessage.getId(),
                    chatMessage.getChatRoomId(),
                    chatMessage.getSenderId(),
                    chatMessage.getType(),
                    chatMessage.getContent(),
                    chatMessage.getParentMessageId(),
                    chatMessage.getCreatedAt()
            );
        }
    }
}
