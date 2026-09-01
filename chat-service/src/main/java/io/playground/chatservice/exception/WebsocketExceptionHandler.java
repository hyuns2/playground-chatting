package io.playground.chatservice.exception;

import io.playground.chatservice.infrastructure.websocket.WebsocketErrorMapper;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class WebsocketExceptionHandler {
    @MessageExceptionHandler(BusinessException.class)
    public ErrorResponseDto handle(BusinessException e) {
        return WebsocketErrorMapper.map(e);
    }

    @MessageExceptionHandler(Exception.class)
    public ErrorResponseDto handle(Exception e) {
        return WebsocketErrorMapper.map(e);
    }
}
