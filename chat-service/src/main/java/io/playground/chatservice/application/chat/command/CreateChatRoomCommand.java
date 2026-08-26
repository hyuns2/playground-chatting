package io.playground.chatservice.application.chat.command;

import io.playground.chatservice.domain.chat.room.ChatRoom;
import io.playground.chatservice.presentation.web.ChatRequestDto;

import java.util.List;

public record CreateChatRoomCommand(
        String creatorId,
        ChatRoom.RoomType type,
        String name,
        List<String> participantIds
) {
    public static CreateChatRoomCommand of(String creatorId,
                                           ChatRoom.RoomType type,
                                           String name,
                                           List<String> participantIds
    ) {
        return new CreateChatRoomCommand(
                creatorId,
                type,
                name,
                participantIds
        );
    }

    public static CreateChatRoomCommand from(String userId,
                                             ChatRequestDto.CreateChatRoom dto) {
        return new CreateChatRoomCommand(
                userId,
                dto.getType(),
                dto.getName(),
                dto.getParticipantIds()
        );
    }
}
