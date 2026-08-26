package io.playground.authservice.domain.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceType {
    WEB(0),
    ANDROID(1),
    IOS(2);

    private final int code;
}
