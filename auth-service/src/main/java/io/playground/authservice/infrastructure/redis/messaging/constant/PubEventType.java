package io.playground.authservice.infrastructure.redis.messaging.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PubEventType {
    AUTH_SIGN_UP_EVENT(0, "auth-sign-up-event");

    private final int code;
    private final String value;
}
