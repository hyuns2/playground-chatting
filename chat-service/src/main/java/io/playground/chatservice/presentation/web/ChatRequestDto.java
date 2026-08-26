package io.playground.chatservice.presentation.web;

import io.playground.chatservice.domain.chat.message.ChatMessage;
import io.playground.chatservice.domain.chat.room.ChatRoom;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ChatRequestDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateChatRoom {
        @NotNull
        ChatRoom.RoomType type;

        String name;

        @NotEmpty
        List<String> participantIds;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendChatMessage {
        @NotBlank
        String senderId;

        @NotNull
        Long chatRoomId;

        @NotNull
        ChatMessage.MessageType type;

        String content;

        Long parentMessageId;
    }
}
