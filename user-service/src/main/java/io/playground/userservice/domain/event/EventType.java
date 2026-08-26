package io.playground.userservice.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    // pub
    USER_PROFILE_CREATED_SUCCESS_EVENT(0, "user-profile-created-success-event"),
    USER_PROFILE_CREATED_FAILURE_EVENT(1, "user-profile-created-failure-event"),
    USER_PROFILE_CREATED(2, "user.profile.created"),

    // sub
    AUTH_SIGN_UP_EVENT(3, "auth-sign-up-event");

    private final int code;
    private final String value;
}
