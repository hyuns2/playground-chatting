package io.playground.chatservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BusinessErrorCode {
    // 400 Bad Request
    INVALID_CHAT_PARTICIPANT("CHAT-400", "해당 채팅방과 관련이 없는 유저입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PARENT_MESSAGE("CHAT-400", "참고할 채팅 메시지가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),

    // 404 Not Found
    PARTICIPANT_NOT_FOUND("CHAT-404", "특정 참여자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 500
    UNKNOWN_EVENT_TYPE("CHAT-500", "알 수 없는 이벤트 타입입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
