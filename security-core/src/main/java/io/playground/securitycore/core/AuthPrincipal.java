package io.playground.securitycore.core;

public record AuthPrincipal(
        String userId,
        String deviceId
) {
    public static AuthPrincipal of(String userId, String deviceId) {
            return new AuthPrincipal(userId, deviceId);
    }
}
