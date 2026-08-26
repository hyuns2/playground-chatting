package io.playground.chatservice.application.chat.command;

import io.playground.chatservice.domain.chat.message.ChatMessage;
import io.playground.chatservice.presentation.web.ChatRequestDto;

import java.time.LocalDateTime;

public record SendChatMessageCommand(
        String senderId,
        Long chatRoomId,
        ChatMessage.MessageType type,
        String content,
        Long parentMessageId,
        LocalDateTime createdAt
) {
    public static SendChatMessageCommand of(String senderId,
                                            Long chatRoomId,
                                            ChatMessage.MessageType type,
                                            String content,
                                            Long parentMessageId) {
        return new SendChatMessageCommand(
                senderId,
                chatRoomId,
                type,
                content,
                parentMessageId,
                LocalDateTime.now()
        );
    }

    public static SendChatMessageCommand from(ChatRequestDto.SendChatMessage dto) {
        return new SendChatMessageCommand(
                dto.getSenderId(),
                dto.getChatRoomId(),
                dto.getType(),
                dto.getContent(),
                dto.getParentMessageId(),
                LocalDateTime.now()
        );
    }
}
