package io.playground.chatservice.application.chat.service;

import io.playground.chatservice.application.chat.command.CreateChatRoomCommand;
import io.playground.chatservice.application.chat.dto.ChatDto;

import java.util.List;

public interface ChatRoomUsecase {
    Long createChatRoom(CreateChatRoomCommand command);

    List<ChatDto.ChatRoomInfo> getChatRooms(String userId);

    List<Long> getChatRoomIds(String userId);
}
