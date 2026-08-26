package io.playground.chatservice.application.read.handler;

import io.playground.chatservice.application.chat.handler.EventHandler;
import io.playground.chatservice.application.chat.handler.MessageHandler;
import io.playground.chatservice.application.read.model.UserProfileCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserViewHandler implements MessageHandler<UserProfileCreatedEvent> {
    private final EventHandler eventHandler;

    @Override
    public void handle(UserProfileCreatedEvent message) {
        eventHandler.handleUserProfileCreatedEvent(message);
    }
}
