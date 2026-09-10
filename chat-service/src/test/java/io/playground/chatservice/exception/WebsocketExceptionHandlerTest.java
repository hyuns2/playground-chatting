package io.playground.chatservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebsocketExceptionHandlerTest {

    private final WebsocketExceptionHandler sut = new WebsocketExceptionHandler();

    @Test
    @DisplayName("BusinessException은 errorCode와 details를 그대로 담은 ErrorResponseDto로 변환된다")
    void handlesBusinessException() {
        BusinessException exception = new BusinessException(
                BusinessErrorCode.INVALID_PARENT_MESSAGE, "parent-id"
        );

        ErrorResponseDto result = sut.handle(exception);

        assertThat(result.code()).isEqualTo(BusinessErrorCode.INVALID_PARENT_MESSAGE.getCode());
        assertThat(result.details()).isEqualTo("parent-id");
        assertThat(result.httpStatus()).isEqualTo(BusinessErrorCode.INVALID_PARENT_MESSAGE.getHttpStatus());
    }

    @Test
    @DisplayName("그 외 예외는 500 계열의 고정된 에러 응답으로 변환된다")
    void handlesUnexpectedException() {
        ErrorResponseDto result = sut.handle(new RuntimeException("boom"));

        assertThat(result.code()).isEqualTo("USER-500A");
    }
}
