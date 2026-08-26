package io.playground.userservice.domain.event;

import lombok.Builder;

@Builder
public record UserProfileCreatedSuccessEvent(
        String userId
) {
}
