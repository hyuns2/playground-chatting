package io.playground.chatservice.application.read.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserView {
    private String userId;
    private String nickName;
    private boolean pushAgree;
    private LocalDateTime updatedAt;

    public static UserView of(String userId,
                              String nickName,
                              boolean pushAgree,
                              LocalDateTime updatedAt) {
        return new UserView(
                userId,
                nickName,
                pushAgree,
                updatedAt
        );
    }

    public void update(String nickName,
                       boolean pushAgree,
                       LocalDateTime updatedAt) {
        if (updatedAt == null ||
                updatedAt.isAfter(this.updatedAt)) {
            this.nickName = nickName;
            this.pushAgree = pushAgree;
            this.updatedAt = updatedAt;
        }
    }
}
