package io.playground.chatservice.infrastructure.persistence.chat.dto;

import io.playground.chatservice.domain.chat.room.ChatRoom;

import java.time.LocalDateTime;

public record ChatFlatInfo(
        Long chatRoomId,
        ChatRoom.RoomType type,
        String name,
        LocalDateTime lastMessagedAt,
        String participantId,
        String nickName,
        boolean isAdmin
) {
}
