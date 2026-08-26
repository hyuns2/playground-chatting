package io.playground.authservice.infrastructure.redis.persistence;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KeyName {
    USER_PREFIX(0, "user:"),
    DEVICE_PREFIX(1, "device:"),
    REFRESH_TOKEN(2, ":refreshToken");

    private final int code;
    private final String value;
}
