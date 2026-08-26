package io.playground.userservice.application.service;

import io.playground.userservice.application.command.CreateUserProfileCommand;
import io.playground.userservice.application.port.EventPublisherPort;
import io.playground.userservice.domain.event.AuthSignUpEvent;
import io.playground.userservice.domain.event.UserProfileCreatedFailureEvent;
import io.playground.userservice.domain.event.UserProfileCreatedSuccessEvent;
import io.playground.userservice.domain.event.UserProfileCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventHandler implements EventUsecase {
    private final UserUsecase userUseCase;
    private final EventPublisherPort eventPublisherPort;

    @Override
    @Transactional
    public void handleAuthSignUpEvent(String eventId,
                                      LocalDateTime eventCreatedAt,
                                      AuthSignUpEvent event) {
        try {
            userUseCase.createUserProfile(
                    CreateUserProfileCommand.builder()
                            .userId(event.userId())
                            .name(event.name())
                            .pushAgree(event.pushAgree())
                            .build()
            );

            eventPublisherPort.publish(
                    UserProfileCreatedSuccessEvent.builder()
                            .userId(event.userId())
                            .build()
            );

            eventPublisherPort.publish(
                    UserProfileCreatedEvent.builder()
                            .userId(event.userId())
                            .nickName(event.name())
                            .pushAgree(event.pushAgree())
                            .createdAt(eventCreatedAt)
                            .build()
            );
        } catch (Exception e) {
            eventPublisherPort.publish(
                    UserProfileCreatedFailureEvent.builder()
                            .userId(event.userId())
                            .build()
            );
        }
    }
}
