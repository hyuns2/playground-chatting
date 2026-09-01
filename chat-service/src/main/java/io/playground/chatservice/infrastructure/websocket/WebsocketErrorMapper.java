package io.playground.chatservice.infrastructure.websocket;

import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.exception.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

@Slf4j
public class WebsocketErrorMapper {
    public static ErrorResponseDto map(Throwable ex) {
        switch (ex) {
            case BusinessException e -> {
                return new ErrorResponseDto(
                        e.getErrorCode(),
                        e.getDetails()
                );
            }
            case AuthenticationException e -> {
                return new ErrorResponseDto(
                        "AUTH-401",
                        "인증에 실패했습니다.",
                        e.getMessage(),
                        HttpStatus.UNAUTHORIZED
                );
            }
            case AccessDeniedException e -> {
                return new ErrorResponseDto(
                        "AUTH-403",
                        "권한이 없습니다.",
                        e.getMessage(),
                        HttpStatus.FORBIDDEN
                );
            }
            default -> {
                log.error(
                        "Unexpected exception occurred while processing webSocket message.",
                        ex
                );

                return new ErrorResponseDto(
                        "USER-500A",
                        "서버 오류가 발생했습니다.",
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        }
    }
}
