package io.playground.chatservice.application.read.command;

import io.playground.chatservice.application.read.model.UserProfileCreatedEvent;

import java.time.LocalDateTime;

public record HandleUserProfileUpdatedCommand(
        String userId,
        String nickName,
        boolean pushAgree,
        LocalDateTime createdAt
) {
    public static HandleUserProfileUpdatedCommand from(UserProfileCreatedEvent event) {
        return new HandleUserProfileUpdatedCommand(
                event.userId(),
                event.nickName(),
                event.pushAgree(),
                event.createdAt()
        );
    }
}
