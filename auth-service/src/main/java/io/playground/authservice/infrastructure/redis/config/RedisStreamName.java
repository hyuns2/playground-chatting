package io.playground.authservice.infrastructure.redis.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisStreamName {
    AUTH_STREAM(0, "auth.events"),
    USER_STREAM(1, "user.events"),
    CHAT_STREAM(2, "chat.events");

    private final int code;
    private final String value;
}
