package io.playground.authservice.domain.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleType {
    USER(0, 1L),
    ADMIN(1, 2L);

    private final int code;
    private final Long id;
}
