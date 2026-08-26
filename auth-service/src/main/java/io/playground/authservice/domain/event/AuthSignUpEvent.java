package io.playground.authservice.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthSignUpEvent {
    String userId;
    String name;
    boolean pushAgree;

    public static AuthSignUpEvent of(String userId, String name, boolean pushAgree) {
        return AuthSignUpEvent.builder()
                .userId(userId)
                .name(name)
                .pushAgree(pushAgree)
                .build();
    }
}
