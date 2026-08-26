package io.playground.chatservice.application.read.model;

import java.time.LocalDateTime;

public record UserProfileCreatedEvent(
        String userId,
        String nickName,
        boolean pushAgree,
        LocalDateTime createdAt
) {
}
