package io.playground.authservice.domain.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE(0),
    BLOCKED(1),
    PENDING(2),
    DELETED(3);

    private final int code;
}
