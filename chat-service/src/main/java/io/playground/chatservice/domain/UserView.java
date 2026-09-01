package io.playground.chatservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserView {
    private Long id;
    private Long userId;
    private String nickname;
    private Instant updatedAt;

    public static UserView of(Long id,
                              Long userId,
                              String nickname,
                              Instant updatedAt) {
        return new UserView(
                id,
                userId,
                nickname,
                updatedAt
        );
    }
}
