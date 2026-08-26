package io.playground.chatservice.infrastructure.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.chatservice.application.chat.dto.ChatDto;
import io.playground.chatservice.application.chat.handler.MessageHandler;
import io.playground.chatservice.domain.chat.message.ChatMessageSentEvent;
import io.playground.chatservice.exception.CustomErrorCode;
import io.playground.chatservice.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageHandlerForPub implements MessageHandler<ChatMessageSentEvent> {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(ChatMessageSentEvent event) {
        try {
            redisTemplate.convertAndSend(
                    "/broadcast/chat",
                    objectMapper.writeValueAsString(ChatDto.ChatMessageInfo.from(event))
            );
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.PUBLISHING_FAILED);
        }
    }
}
