package io.playground.chatservice.infrastructure.websocket;

import io.playground.chatservice.application.chat.manager.WebSocketSessionManager;
import io.playground.chatservice.application.chat.service.ChatRoomUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketSessionHandler {
    private final ChatRoomUsecase chatRoomUsecase;
    private final WebSocketSessionManager sessionManager;

    public void onConnect(String userId, String deviceId) {
        List<Long> chatRoomIds = chatRoomUsecase.getChatRoomIds(userId);

        sessionManager.addSession(
                userId,
                deviceId,
                chatRoomIds
        );
    }

    public void onDisconnect(String userId, String deviceId) {
        sessionManager.removeSession(
                userId,
                deviceId
        );
    }
}
