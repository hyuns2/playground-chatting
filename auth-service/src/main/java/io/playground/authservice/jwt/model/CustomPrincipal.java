package io.playground.authservice.jwt.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomPrincipal {
    private final String userId;
    private final String deviceId;

    public static CustomPrincipal of(String userId, String deviceId) {
        return CustomPrincipal.builder()
                .userId(userId)
                .deviceId(deviceId)
                .build();
    }
}
