package io.playground.userservice.application.command;

import lombok.Builder;

@Builder
public record CreateUserProfileCommand(
        String userId,
        String name,
        boolean pushAgree
) {
}
