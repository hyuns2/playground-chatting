package io.playground.authservice.infrastructure.redis.messaging.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubEventType {
    USER_PROFILE_CREATED_SUCCESS_EVENT(1, "user-profile-created-success-event"),
    USER_PROFILE_CREATED_FAILURE_EVENT(2, "user-profile-created-failure-event");

    private final int code;
    private final String value;
}
