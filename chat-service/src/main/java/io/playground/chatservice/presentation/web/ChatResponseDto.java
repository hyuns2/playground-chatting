package io.playground.chatservice.presentation.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.playground.chatservice.application.chat.dto.ChatDto;
import io.playground.chatservice.domain.chat.message.ChatMessage;
import io.playground.chatservice.domain.chat.room.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ChatResponseDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetChatRoomInfo {
        Long id;

        ChatRoom.RoomType type;

        String name;

        LocalDateTime lastMessagedAt;

        List<ParticipantInfo> participantInfos;

        public static GetChatRoomInfo from(ChatDto.ChatRoomInfo dto) {
            return new GetChatRoomInfo(
                    dto.getChatRoomId(),
                    dto.getType(),
                    dto.getName(),
                    dto.getLastMessagedAt(),
                    dto.getParticipantInfos().stream()
                            .map(ParticipantInfo::from)
                            .toList()
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ParticipantInfo {
        String id;

        String nickName;

        @JsonProperty("isAdmin")
        boolean isAdmin;

        private static ParticipantInfo from(ChatDto.ParticipantInfo info) {
            return new ParticipantInfo(
                    info.getId(),
                    info.getNickName(),
                    info.isAdmin()
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetChatMessagesInfo {
        Map<String, Long> lastReadMessageIdInfos;

        List<ChatMessageInfo> chatMessageInfoInfos;

        String nextCursor;

        public static GetChatMessagesInfo from(ChatDto.ChatMessagesInfo result) {
            return new GetChatMessagesInfo(
                    result.getLastReadMessageIdInfos(),
                    result.getChatMessageInfos().stream()
                            .map(ChatResponseDto.ChatMessageInfo::from)
                            .toList(),
                    result.getNextCursor()
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ChatMessageInfo {
        Long chatMessageId;

        String senderId;

        ChatMessage.MessageType type;

        String content;

        Long parentMessageId;

        LocalDateTime createdAt;

        public static ChatMessageInfo from(ChatDto.ChatMessageInfo info) {
            return new ChatMessageInfo(
                    info.getChatMessageId(),
                    info.getSenderId(),
                    info.getType(),
                    info.getContent(),
                    info.getParentMessageId(),
                    info.getCreatedAt()
            );
        }
    }
}
