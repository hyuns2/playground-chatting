package io.playground.userservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfile {
    private String userId;

    private String name;

    private String nickName;

    private boolean pushAgree;

    public static UserProfile of(String userId, String name, boolean pushAgree) {
        return new UserProfile(userId, name, name, pushAgree);
    }
}
