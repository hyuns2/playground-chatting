package io.playground.chatservice.application.eventstream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PubEventType {
    CHAT_MESSAGE_SENT(0, "chat.message.sent");

    private final int code;
    private final String value;
}
