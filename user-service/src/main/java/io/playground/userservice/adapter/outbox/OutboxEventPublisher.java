package io.playground.userservice.adapter.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.userservice.application.port.EventPublisherPort;
import io.playground.userservice.common.exception.CustomErrorCode;
import io.playground.userservice.common.exception.CustomException;
import io.playground.userservice.domain.event.EventType;
import io.playground.userservice.domain.event.UserProfileCreatedFailureEvent;
import io.playground.userservice.domain.event.UserProfileCreatedSuccessEvent;
import io.playground.userservice.domain.event.UserProfileCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher implements EventPublisherPort {
    private final EventLogJpaRepository eventLogJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(Object event) {
        if (UserProfileCreatedSuccessEvent.class.equals(event.getClass()))
            handle(EventType.USER_PROFILE_CREATED_SUCCESS_EVENT, event);

        else if (UserProfileCreatedFailureEvent.class.equals(event.getClass()))
            handle(EventType.USER_PROFILE_CREATED_FAILURE_EVENT, event);

        else if (UserProfileCreatedEvent.class.equals(event.getClass()))
            handle(EventType.USER_PROFILE_CREATED, event);

        else
            throw new CustomException(CustomErrorCode.UNKNOWN_EVENT_TYPE);
    }

    private void handle(EventType eventType, Object event) {
        try {
            eventLogJpaRepository.save(
                    EventLogEntity.builder()
                            .eventType(eventType)
                            .payload(objectMapper.writeValueAsString(event))
                            .build()
            );
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.PUBLISHING_FAILED);
        }
    }
}
