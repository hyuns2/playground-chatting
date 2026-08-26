package io.playground.chatservice.exception;

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

    // 400
    CHAT_ROOM_NOT_FOUND("Chatting-001", "해당하는 채팅방을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    PARTICIPANT_NOT_FOUND("Chatting-002", "특정 유저를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),

    // 404
    ONLY_DIRECT_CHAT_SUPPORTED("Chatting-003", "1:1 채팅만 지원하는 기능입니다.", HttpStatus.BAD_REQUEST),
    INVALID_CHAT_PARTICIPANT("Chatting-004", "해당 채팅방과 관련이 없는 유저입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PARENT_MESSAGE("chatting-005", "참고할 채팅 메시지가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),

    // stream
    PRODUCING_FAILED("Stream-001", "이벤트 발행에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CONSUMING_FAILED("Stream-002", "이벤트 수신에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    UNKNOWN_EVENT_TYPE("Stream-003", "알 수 없는 이벤트 타입입니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // pub/sub
    PUBLISHING_FAILED("Stream-001", "메시지 발행에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    SUBSCRIBING_FAILED("Stream-002", "메시지 구독에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
