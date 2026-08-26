package io.playground.authservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {
    private final CustomErrorCode errorCode;

    @Override
    public String getMessage() {
        return getErrorCode().name() + ": " + getErrorCode().getMessage();
    }
}
