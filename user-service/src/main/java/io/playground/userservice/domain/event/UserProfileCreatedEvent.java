package io.playground.userservice.domain.event;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserProfileCreatedEvent(
        String userId,
        String nickName,
        boolean pushAgree,
        LocalDateTime createdAt
) {
}
