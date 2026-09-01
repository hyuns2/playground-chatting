package io.playground.chatservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ChatRoom {
    private Long id;

    private RoomType type;

    private String name;

    private Instant lastMessagedAt;

    public enum RoomType {
        PRIVATE, GROUP
    }

    public static ChatRoom of(Long id,
                              RoomType type,
                              String name,
                              Instant lastMessagedAt) {
        return new ChatRoom(
                id,
                type,
                name,
                lastMessagedAt
        );
    }
}
