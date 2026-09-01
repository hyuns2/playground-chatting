package io.playground.chatservice.presentation;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.domain.ChatMessage;
import io.playground.chatservice.domain.ChatRoom;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class ChatRequestDto {
    public record CreateChatRoom(
        @NotNull ChatRoom.RoomType type,
        @NotEmpty List<Long> participantIds,
        String name
    ) {
    }

    public record SendChatMessage(
            @NotNull ChatMessage.MessageType type,
            @NotBlank String content,
            Long parentMessageId
    ) {
    }

    public record ChatMessageCursor(
            @NotNull Long id,
            @NotNull LocalDateTime createdAt
    ) {
        public ChatQueryDto.ChatMessageCursor toQueryDto() {
            return new ChatQueryDto.ChatMessageCursor(
                    id,
                    createdAt.atZone(
                            ZoneId.systemDefault()
                    ).toInstant()
            );
        }
    }
}
