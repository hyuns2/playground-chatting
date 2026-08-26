package io.playground.userservice.adapter.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.userservice.common.config.RedisStreamConfig;
import io.playground.userservice.common.config.RedisStreamName;
import io.playground.userservice.common.exception.CustomErrorCode;
import io.playground.userservice.common.exception.CustomException;
import io.playground.userservice.domain.event.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisEventConsumer implements StreamListener<String, MapRecord<String, String, String>> {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisEventHandler eventHandler;
    private final static String USER_STREAM_NAME = RedisStreamName.USER_STREAM.getValue();
    private final static String USER_GROUP_NAME = RedisStreamConfig.USER_GROUP_NAME;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        handleMessage(message);

        reclaimPendingMessages();
    }

    private void handleMessage(MapRecord<String, String, String> message) {
        try {
            EventEnvelope envelope = objectMapper.convertValue(message.getValue(), EventEnvelope.class);

            if (envelope.getEventType().equals(EventType.AUTH_SIGN_UP_EVENT.getValue()))
                eventHandler.handleAuthSignUpEvent(envelope);

            else
                throw new CustomException(CustomErrorCode.UNKNOWN_EVENT_TYPE);
            ack(message);
        } catch (CustomException e) {
            ack(message);
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ack(MapRecord<String, String, String> message) {
        stringRedisTemplate.opsForStream()
                .acknowledge(
                        USER_STREAM_NAME,
                        USER_GROUP_NAME,
                        message.getId()
                );
    }

    /**
     * ToDo: Pending 메시지 재처리 로직 개선 필요
     * consume 1회 -> pending 메시지 10개 처리
     * retry 횟수로 재처리 여부 확인 + dlq 활용
     */
    private void reclaimPendingMessages() {
        PendingMessages pendingMessages = stringRedisTemplate.opsForStream()
                .pending(
                        USER_STREAM_NAME,
                        USER_GROUP_NAME,
                        Range.unbounded(), 20
                );

        // Todo: 현재 claim x -> 매 소비마다 팬딩 일부를 수동 처리 상황
        for (PendingMessage pendingMessage : pendingMessages.get().toList()) {
            String id = pendingMessage.getId().getValue();

            List<MapRecord<String, String, String>> messages =
                    stringRedisTemplate.<String, String>opsForStream()
                            .range(
                                    USER_STREAM_NAME,
                                    Range.closed(id, id)
                            );

            for (MapRecord<String, String, String> message : messages)
                handleMessage(message);
        }
    }
}
