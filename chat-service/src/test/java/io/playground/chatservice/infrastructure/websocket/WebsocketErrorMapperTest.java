package io.playground.chatservice.infrastructure.websocket;

import io.playground.chatservice.exception.BusinessErrorCode;
import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.exception.ErrorResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class WebsocketErrorMapperTest {

    @Test
    @DisplayName("BusinessException은 errorCode와 details를 그대로 매핑한다")
    void mapsBusinessException() {
        BusinessException exception = new BusinessException(
                BusinessErrorCode.INVALID_CHAT_PARTICIPANT, "room-1"
        );

        ErrorResponseDto result = WebsocketErrorMapper.map(exception);

        assertThat(result.code()).isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT.getCode());
        assertThat(result.message()).isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT.getMessage());
        assertThat(result.details()).isEqualTo("room-1");
        assertThat(result.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("AuthenticationException은 401 AUTH-401로 매핑한다")
    void mapsAuthenticationException() {
        ErrorResponseDto result = WebsocketErrorMapper.map(new BadCredentialsException("bad token"));

        assertThat(result.code()).isEqualTo("AUTH-401");
        assertThat(result.details()).isEqualTo("bad token");
        assertThat(result.httpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("AccessDeniedException은 403 AUTH-403으로 매핑한다")
    void mapsAccessDeniedException() {
        ErrorResponseDto result = WebsocketErrorMapper.map(new AccessDeniedException("no access"));

        assertThat(result.code()).isEqualTo("AUTH-403");
        assertThat(result.details()).isEqualTo("no access");
        assertThat(result.httpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("그 외 예외는 500 USER-500A로 매핑되고 details는 노출하지 않는다")
    void mapsUnexpectedException() {
        ErrorResponseDto result = WebsocketErrorMapper.map(new IllegalStateException("internal detail"));

        assertThat(result.code()).isEqualTo("USER-500A");
        assertThat(result.details()).isNull();
        assertThat(result.httpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
