package io.playground.chatservice.application.eventstream;

public interface EventStreamNamingStrategy {
    String toStreamName(EventStreamType streamType, String key);

    String toGroupName(String streamName, EventGroupType groupType);
}
