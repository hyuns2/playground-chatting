package io.playground.authservice.jwt.model;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class JwtToken {
    private String grantType;
    private String accessToken;
    private String refreshToken;

    public static JwtToken of(String grantType, String accessToken, String refreshToken) {
        return JwtToken.builder()
                .grantType(grantType)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
