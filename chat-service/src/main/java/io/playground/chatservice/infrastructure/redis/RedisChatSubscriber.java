package io.playground.chatservice.infrastructure.redis;

import io.playground.chatservice.application.event.ChatEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class RedisChatSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        ChatEventDto.ChatMessageSent dto =
                objectMapper.readValue(
                        message.getBody(),
                        ChatEventDto.ChatMessageSent.class
                );

        messagingTemplate.convertAndSend(
                "/sub/room/" + dto.chatRoomId(),
                dto
        );
    }
}
