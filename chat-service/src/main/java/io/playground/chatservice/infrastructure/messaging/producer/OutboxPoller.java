package io.playground.chatservice.infrastructure.messaging.producer;

import io.playground.chatservice.application.eventstream.EventStreamNamingStrategy;
import io.playground.chatservice.application.eventstream.EventStreamType;
import io.playground.chatservice.exception.CustomErrorCode;
import io.playground.chatservice.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPoller {
    private final EventLogJpaRepository eventLogJpaRepository;
    private final RedisEventPublisher redisEventPublisher;
    private final EventStreamNamingStrategy namingStrategy;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {
        List<EventLogJpaEntity> eventLogs = eventLogJpaRepository.findAllByProcessedFalse();

        for (EventLogJpaEntity eventLog : eventLogs) {
            try {
                switch (eventLog.getEventType()) {
                    case CHAT_MESSAGE_SENT ->
                            redisEventPublisher.publish(
                                    namingStrategy.toStreamName(EventStreamType.CHAT_MESSAGES, eventLog.getStreamKey()),
                                    eventLog.toEventEnvelope().toMap()
                            );
                    default -> throw new CustomException(CustomErrorCode.UNKNOWN_EVENT_TYPE);
                }

                eventLog.setProcessed(true);
            } catch (Exception e) {
                eventLog.setProcessed(false);
            }
        }
    }
}
