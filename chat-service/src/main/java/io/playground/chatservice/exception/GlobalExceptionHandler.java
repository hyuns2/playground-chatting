package io.playground.chatservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponseDto> handleException(BusinessException e) {
        return ResponseEntity
                .status(
                        e.getErrorCode()
                                .getHttpStatus()
                ).body(
                        new ErrorResponseDto(
                                e.getErrorCode(),
                                e.getDetails()
                        )
                );
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponseDto> handleException(Exception e,
                                                               HttpServletRequest request) {
        log.error(
                "Unexpected error. uri={}, method={}",
                request.getRequestURI(),
                request.getMethod(),
                e
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ErrorResponseDto(
                                "CHAT-500",
                                "서버 오류가 발생했습니다.",
                                e.getMessage(),
                                HttpStatus.INTERNAL_SERVER_ERROR
                        )
                );
    }
}
