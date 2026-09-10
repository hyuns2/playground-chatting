package io.playground.chatservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler sut = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessException은 errorCode의 HttpStatus와 details를 그대로 응답한다")
    void handlesBusinessException() {
        BusinessException exception = new BusinessException(
                BusinessErrorCode.INVALID_CHAT_PARTICIPANT, "detail-info"
        );

        ResponseEntity<ErrorResponseDto> response = sut.handleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT.getCode());
        assertThat(response.getBody().message()).isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT.getMessage());
        assertThat(response.getBody().details()).isEqualTo("detail-info");
    }

    @Test
    @DisplayName("예상치 못한 예외는 500과 고정된 CHAT-500 코드로 응답한다")
    void handlesUnexpectedException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/room/1/messages/cursor");
        when(request.getMethod()).thenReturn("POST");
        RuntimeException exception = new RuntimeException("boom");

        ResponseEntity<ErrorResponseDto> response = sut.handleException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("CHAT-500");
        assertThat(response.getBody().details()).isEqualTo("boom");
    }
}
