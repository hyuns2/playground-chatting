package io.playground.chatservice.application.eventstream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventGroupType {
    VIEW_UPDATER(":cg:view-updater"),
    MESSAGE_PUBLISHER(":cg:message-publisher");

    private final String value;
}
