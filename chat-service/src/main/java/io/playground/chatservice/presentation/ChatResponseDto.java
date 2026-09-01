package io.playground.chatservice.presentation;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.domain.ChatMessage;
import io.playground.chatservice.domain.ChatRoom;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class ChatResponseDto {
    public record GetChatRoomInfo(
            Long id,
            ChatRoom.RoomType type,
            String name,
            LocalDateTime lastMessagedAt,
            List<ParticipantInfo> participantInfos
    ) {
        public static GetChatRoomInfo from(ChatRoom chatRoom,
                                           List<ParticipantInfo> participantInfos) {
            return new GetChatRoomInfo(
                    chatRoom.getId(),
                    chatRoom.getType(),
                    chatRoom.getName(),
                    chatRoom.getLastMessagedAt() != null ?
                            LocalDateTime.ofInstant(
                                    chatRoom.getLastMessagedAt(),
                                    ZoneId.systemDefault()
                            ) : null,
                    participantInfos
            );
        }
    }

    @Builder
    public record ParticipantInfo(
        Long id,
        String nickname,
        boolean isAdmin
    ) {
    }

    @Builder
    public record GetChatMessagesInfo(
            List<LastReadMessageInfo> lastReadMessageInfos,
            List<ChatMessageInfo> chatMessageInfos,
            NextChatMessageCursor nextCursor
    ) {
    }

    @Builder
    public record LastReadMessageInfo(
            Long participantId,
            Long lastReadMessageId
    ) {
        public static LastReadMessageInfo from(ChatQueryDto.LastReadMessageInfo dto) {
            return new LastReadMessageInfo(
                    dto.participantId(),
                    dto.lastReadMessageId()
            );
        }
    }

    public record ChatMessageInfo(
            Long id,
            Long senderId,
            ChatMessage.MessageType type,
            String content,
            Long parentMessageId,
            LocalDateTime createdAt
    ) {
        public static ChatMessageInfo from(ChatMessage chatMessage) {
            return new ChatMessageInfo(
                    chatMessage.getId(),
                    chatMessage.getSenderId(),
                    chatMessage.getType(),
                    chatMessage.getContent(),
                    chatMessage.getParentMessageId(),
                    LocalDateTime.ofInstant(
                            chatMessage.getCreatedAt(),
                            ZoneId.systemDefault()
                    )
            );
        }
    }

    @Builder
    public record NextChatMessageCursor(
            Long id,
            LocalDateTime createdAt,
            boolean hasNext
    ) {
    }
}
