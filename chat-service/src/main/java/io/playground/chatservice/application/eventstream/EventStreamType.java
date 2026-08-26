package io.playground.chatservice.application.eventstream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStreamType {
    CHAT_EVENTS("chat:events"),
    CHAT_MESSAGES("chat:messages:room");

    private final String prefix;
}
