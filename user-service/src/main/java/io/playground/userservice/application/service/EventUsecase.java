package io.playground.userservice.application.service;

import io.playground.userservice.domain.event.AuthSignUpEvent;

import java.time.LocalDateTime;

public interface EventUsecase {
    void handleAuthSignUpEvent(String eventId, LocalDateTime eventCreatedAt, AuthSignUpEvent event);
}
