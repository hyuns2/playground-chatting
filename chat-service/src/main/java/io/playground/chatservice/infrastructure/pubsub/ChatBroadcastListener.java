package io.playground.chatservice.infrastructure.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.chatservice.application.chat.dto.ChatDto;
import io.playground.chatservice.exception.CustomErrorCode;
import io.playground.chatservice.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatBroadcastListener implements MessageListener {
    private final SimpMessageSendingOperations messageSendingOps;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody());
        ChatDto.ChatMessageInfo dto;

        try {
            if (payload.startsWith("\""))
                payload = objectMapper.readValue(payload, String.class);

            dto = objectMapper.readValue(
                    payload,
                    ChatDto.ChatMessageInfo.class
            );
        } catch (Exception e) {
            e.printStackTrace();

            throw new CustomException(CustomErrorCode.SUBSCRIBING_FAILED);
        }

        messageSendingOps.convertAndSend(
                "/sub/chat-room/" + dto.getChatRoomId(),
                dto
        );
    }
}
