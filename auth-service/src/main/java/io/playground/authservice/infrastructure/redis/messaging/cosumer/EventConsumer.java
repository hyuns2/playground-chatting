package io.playground.authservice.infrastructure.redis.messaging.cosumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.authservice.exception.CustomErrorCode;
import io.playground.authservice.exception.CustomException;
import io.playground.authservice.infrastructure.redis.config.RedisStreamConfig;
import io.playground.authservice.infrastructure.redis.config.RedisStreamName;
import io.playground.authservice.infrastructure.redis.messaging.constant.EventEnvelope;
import io.playground.authservice.infrastructure.redis.messaging.constant.SubEventType;
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
public class EventConsumer implements StreamListener<String, MapRecord<String, String, String>> {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final EventHandler eventHandler;
    private final static String AUTH_STREAM_NAME = RedisStreamName.AUTH_STREAM.getValue();
    private final static String AUTH_GROUP_NAME = RedisStreamConfig.AUTH_GROUP_NAME;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        handleMessage(message);

        reclaimPendingMessages();
    }

    private void handleMessage(MapRecord<String, String, String> message) {
        try {
            EventEnvelope envelope = objectMapper.convertValue(message.getValue(), EventEnvelope.class);

            if (envelope.getEventType().equals(SubEventType.USER_PROFILE_CREATED_SUCCESS_EVENT.getValue()))
                eventHandler.handleUserProfileCreatedSuccessEvent(envelope);
            else if (envelope.getEventType().equals(SubEventType.USER_PROFILE_CREATED_FAILURE_EVENT.getValue()))
                eventHandler.handleUserProfileCreatedFailureEvent(envelope);
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
                        AUTH_STREAM_NAME,
                        AUTH_GROUP_NAME,
                        message.getId()
                );
    }

    /**
     * ToDo: Pending 메시지 재처리 로직 개선 필요
     * consume 1회 -> pending 메시지 10개 처리
     * retry 횟수로 재처리 여부 확인 + dlq 활용
     */
    private void reclaimPendingMessages() {
        // Todo: 현재 claim x -> 매 소비마다 팬딩 전체를 수동 처리 상황
        while (true) {
            PendingMessages pending = stringRedisTemplate.opsForStream()
                    .pending(
                            AUTH_STREAM_NAME,
                            AUTH_GROUP_NAME,
                            Range.unbounded(), 10
                    );

            if (pending.isEmpty())
                break;

            List<PendingMessage> messages = pending.get().toList();
            for (PendingMessage pm : messages) {
                String recordId = pm.getId().getValue();

                List<MapRecord<String, String, String>> records =
                        stringRedisTemplate.<String, String>opsForStream()
                                .range(
                                        AUTH_STREAM_NAME,
                                        Range.closed(recordId, recordId)
                                );

                for (MapRecord<String, String, String> record : records)
                    handleMessage(record);
            }

            break;
        }
    }
}
