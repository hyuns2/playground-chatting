package io.playground.authservice.presentation.dto;

import io.playground.authservice.jwt.model.JwtToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthResponseDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignIn {
        JwtToken jwtToken;
        String deviceId;

        public static SignIn of(JwtToken jwtToken, String deviceId) {
            return SignIn.builder()
                    .jwtToken(jwtToken)
                    .deviceId(deviceId)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Reissue {
        JwtToken jwtToken;

        public static Reissue of(JwtToken jwtToken) {
            return Reissue.builder()
                    .jwtToken(jwtToken)
                    .build();
        }
    }
}
