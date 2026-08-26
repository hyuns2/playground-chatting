package io.playground.chatservice.application.chat.command;

public record GetChatMessagesCommand(
        String userId,
        Long chatRoomId,
        String limit
) {
    public static GetChatMessagesCommand of(String userId,
                                            Long chatRoomId,
                                            String limit) {
        return new GetChatMessagesCommand(
                userId,
                chatRoomId,
                limit
        );
    }
}
