package io.playground.chatservice.application.chat.handler;

public interface MessageHandler<T> {
    void handle(T message);
}
