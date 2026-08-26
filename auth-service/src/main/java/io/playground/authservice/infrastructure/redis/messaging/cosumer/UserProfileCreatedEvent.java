package io.playground.authservice.infrastructure.redis.messaging.cosumer;

public record UserProfileCreatedEvent(
        String userId
) {
}
