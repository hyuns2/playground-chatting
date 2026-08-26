package io.playground.chatservice.application.read.service;

import io.playground.chatservice.application.read.command.HandleUserProfileUpdatedCommand;

public interface UserViewUsecase {
    void handleUserProfileUpdated(HandleUserProfileUpdatedCommand command);
}
