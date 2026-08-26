package io.playground.authservice.domain.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthType {
    GOOGLE(1, "google"),
    NAVER(2, "naver");

    private final int code;
    private final String provider;
}
