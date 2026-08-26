package io.playground.userservice.application.service;

import io.playground.userservice.application.command.CreateUserProfileCommand;

public interface UserUsecase {
    void createUserProfile(CreateUserProfileCommand command);
}
