package io.playground.chatservice.application.chat.handler;

import io.playground.chatservice.application.read.command.HandleUserProfileUpdatedCommand;
import io.playground.chatservice.application.read.model.UserProfileCreatedEvent;
import io.playground.chatservice.application.read.service.UserViewUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventHandler {
    private final UserViewUsecase userViewUsecase;

    public void handleUserProfileCreatedEvent(UserProfileCreatedEvent event) {
        userViewUsecase.handleUserProfileUpdated(
                HandleUserProfileUpdatedCommand.from(event)
        );
    }
}
