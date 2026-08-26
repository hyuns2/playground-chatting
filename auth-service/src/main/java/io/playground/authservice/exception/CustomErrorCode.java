package io.playground.authservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {
    // 0. origin
    UNAUTHORIZED("Origin-401", "인증되지 않은 요청입니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("Origin-403", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    SERVER_ERROR("Origin-500", "알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // security
    INVALID_TOKEN("Auth-001", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_TYPE("Auth-002", "유효하지 않은 토큰 타입입니다.", HttpStatus.BAD_REQUEST),

    // 400
    USER_ALREADY_EXISTS("Auth-101", "해당 이메일로 가입된 유저가 이미 존재합니다.", HttpStatus.BAD_REQUEST),

    // 404
    USER_NOT_FOUND("Auth-201", "해당하는 유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // stream
    PUBLISHING_FAILED("Stream-001", "이벤트 발행에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CONSUMING_FAILED("Stream-002", "이벤트 수신에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    UNKNOWN_EVENT_TYPE("Stream-003", "알 수 없는 이벤트 타입입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
