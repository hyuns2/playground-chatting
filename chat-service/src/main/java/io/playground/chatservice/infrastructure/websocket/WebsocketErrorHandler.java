package io.playground.chatservice.infrastructure.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketErrorHandler extends StompSubProtocolErrorHandler {
    private final ObjectMapper objectMapper;

    @Override
    public @Nullable Message<byte[]> handleClientMessageProcessingError(@Nullable Message<byte[]> clientMessage,
                                                                        Throwable ex) {
        try {
            StompHeaderAccessor accessor =
                    StompHeaderAccessor.create(
                            StompCommand.ERROR
                    );
            accessor.setMessage(ex.getMessage());
            accessor.setContentType(
                    new MimeType(
                            MediaType.APPLICATION_JSON,
                            StandardCharsets.UTF_8
                    )
            );

            byte[] payload = objectMapper.writeValueAsBytes(
                    WebsocketErrorMapper.map(ex)
            );

            return MessageBuilder.createMessage(
                    payload,
                    accessor.getMessageHeaders()
            );
        } catch (Exception e) {
            return super.handleClientMessageProcessingError(
                    clientMessage,
                    ex
            );
        }
    }
}
