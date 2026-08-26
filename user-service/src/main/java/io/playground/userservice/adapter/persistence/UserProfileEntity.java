package io.playground.userservice.adapter.persistence;

import io.playground.userservice.domain.UserProfile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "USER_PROFILE")
public class UserProfileEntity {
    @Id
    private String userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private boolean pushAgree;

    public static UserProfileEntity from(UserProfile userProfile) {
        return UserProfileEntity.builder()
                .userId(userProfile.getUserId())
                .name(userProfile.getName())
                .nickName(userProfile.getNickName())
                .pushAgree(userProfile.isPushAgree())
                .build();
    }
}
