package io.playground.chatservice.application.chat.service;

import io.playground.chatservice.application.chat.command.GetChatMessagesCommand;
import io.playground.chatservice.application.chat.command.SendChatMessageCommand;
import io.playground.chatservice.application.chat.dto.ChatDto;

public interface ChatMessageUsecase {
    Long sendChatMessage(SendChatMessageCommand command);

    ChatDto.ChatMessagesInfo getChatMessagesWithPaging(GetChatMessagesCommand command);

    ChatDto.ChatMessagesInfo getChatMessagesWithCursor(GetChatMessagesCommand command);
}
