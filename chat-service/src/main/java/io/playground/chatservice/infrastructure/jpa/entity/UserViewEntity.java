package io.playground.chatservice.infrastructure.jpa.entity;

import io.playground.chatservice.domain.UserView;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user_views")
public class UserViewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Instant updatedAt;

    public static UserViewEntity from(UserView userView) {
        return new UserViewEntity(
                userView.getId(),
                userView.getUserId(),
                userView.getNickname(),
                userView.getUpdatedAt()
        );
    }

    public UserView toDomain() {
        return UserView.of(
                id,
                userId,
                nickname,
                updatedAt
        );
    }
}
