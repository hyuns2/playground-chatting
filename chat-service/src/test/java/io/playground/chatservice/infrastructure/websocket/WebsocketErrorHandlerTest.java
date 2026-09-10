package io.playground.chatservice.infrastructure.websocket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class WebsocketErrorHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebsocketErrorHandler sut = new WebsocketErrorHandler(objectMapper);

    @Test
    @DisplayName("클라이언트 메시지 처리 중 예외가 발생하면 ERROR 프레임에 매핑된 에러 정보를 JSON으로 담는다")
    void mapsExceptionToErrorFrame() {
        StompHeaderAccessor sendAccessor = StompHeaderAccessor.create(StompCommand.SEND);
        Message<byte[]> clientMessage = MessageBuilder.createMessage(
                new byte[0], sendAccessor.getMessageHeaders()
        );

        Message<byte[]> result = sut.handleClientMessageProcessingError(
                clientMessage, new IllegalStateException("boom")
        );

        assertThat(result).isNotNull();
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getCommand()).isEqualTo(StompCommand.ERROR);
        assertThat(resultAccessor.getMessage()).isEqualTo("boom");

        String json = new String(result.getPayload());
        assertThat(json).contains("\"USER-500A\"");
    }
}
