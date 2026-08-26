package io.playground.userservice.adapter.outbox;

import io.playground.userservice.adapter.messaging.RedisStreamPublisher;
import io.playground.userservice.common.config.RedisStreamName;
import io.playground.userservice.common.exception.CustomErrorCode;
import io.playground.userservice.common.exception.CustomException;
import io.playground.userservice.domain.event.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPoller {
    private final EventLogJpaRepository eventLogJpaRepository;
    private final RedisStreamPublisher redisStreamPublisher;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {
        List<EventLogEntity> eventLogs = eventLogJpaRepository.findAllByProcessedFalse();

        for (EventLogEntity eventLog : eventLogs) {
            try {
                if (eventLog.getEventType().equals(EventType.USER_PROFILE_CREATED_SUCCESS_EVENT))
                    redisStreamPublisher.publish(RedisStreamName.AUTH_STREAM.getValue(), eventLog);

                else if (eventLog.getEventType().equals(EventType.USER_PROFILE_CREATED_FAILURE_EVENT))
                    redisStreamPublisher.publish(RedisStreamName.AUTH_STREAM.getValue(), eventLog);

                else if (eventLog.getEventType().equals(EventType.USER_PROFILE_CREATED))
                    redisStreamPublisher.publish(RedisStreamName.CHAT_STREAM.getValue(), eventLog);

                else
                    throw new CustomException(CustomErrorCode.UNKNOWN_EVENT_TYPE);

                eventLog.setProcessed(true);
            } catch (Exception e) {
                eventLog.setProcessed(false);
            }
        }
    }
}
