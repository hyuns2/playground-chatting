package io.playground.chatservice.application.chat.port;

import io.playground.chatservice.application.eventstream.PubEventType;

public interface EventPublisherPort {
    <T> void publish(PubEventType type, String streamKey, T payload);
}
