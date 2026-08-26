package io.playground.userservice.domain.event;

public record AuthSignUpEvent(
        String userId,
        String name,
        boolean pushAgree
) {
}
