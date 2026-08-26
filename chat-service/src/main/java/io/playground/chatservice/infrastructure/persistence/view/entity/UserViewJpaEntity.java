package io.playground.chatservice.infrastructure.persistence.view.entity;

import io.playground.chatservice.application.read.model.UserView;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "USER_VIEW")
public class UserViewJpaEntity {
    @Id
    private String userId;

    private String nickName;

    private boolean pushAgree;

    private LocalDateTime updatedAt;

    public static UserViewJpaEntity of(String userId,
                                       String nickName,
                                       boolean pushAgree,
                                       LocalDateTime updatedAt) {
        return new UserViewJpaEntity(
                userId,
                nickName,
                pushAgree,
                updatedAt
        );
    }

    public static UserViewJpaEntity ofEmpty() {
        return new UserViewJpaEntity();
    }

    public UserView toDomain() {
        return new UserView(
                this.userId,
                this.nickName,
                this.pushAgree,
                this.updatedAt
        );
    }

    public void update(String userId,
                       String nickName,
                       boolean pushAgree,
                       LocalDateTime updatedAt) {
        this.userId = userId;
        this.nickName = nickName;
        this.pushAgree = pushAgree;
        this.updatedAt = updatedAt;
    }
}
