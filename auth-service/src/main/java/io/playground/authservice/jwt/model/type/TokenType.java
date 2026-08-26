package io.playground.authservice.jwt.model.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {
    ACCESS(0, "access"),
    REFRESH(1, "refresh");

    private final int code;
    private final String type;
}
