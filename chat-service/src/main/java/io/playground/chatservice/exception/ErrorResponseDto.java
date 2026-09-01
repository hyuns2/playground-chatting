package io.playground.chatservice.exception;

import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;

public record ErrorResponseDto(
        String code,
        String message,
        @Nullable String details,
        HttpStatus httpStatus
) {
    public ErrorResponseDto(BusinessErrorCode errorCode,
                            String details) {
        this(
                errorCode.getCode(),
                errorCode.getMessage(),
                details,
                errorCode.getHttpStatus()
        );
    }
}
