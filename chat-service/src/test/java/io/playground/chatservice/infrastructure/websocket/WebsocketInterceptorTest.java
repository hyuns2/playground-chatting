package io.playground.chatservice.infrastructure.websocket;

import io.playground.chatservice.application.usecase.ChatRoomService;
import io.playground.securitycore.jwt.AuthPrincipal;
import io.playground.securitycore.jwt.JwtTokenParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompConversionException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebsocketInterceptorTest {

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private JwtTokenParser jwtTokenParser;

    @Mock
    private MessageChannel channel;

    private WebsocketInterceptor sut;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        sut = new WebsocketInterceptor(chatRoomService, jwtTokenParser, List.of("ACTIVE"));
    }

    private static Message<byte[]> stompMessage(StompCommand command, String authorizationHeader, String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setLeaveMutable(true);
        if (authorizationHeader != null)
            accessor.setNativeHeader("Authorization", authorizationHeader);
        if (destination != null)
            accessor.setDestination(destination);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private AuthPrincipal mockAuthPrincipal(List<String> roles, String status) {
        AuthPrincipal authPrincipal = mock(AuthPrincipal.class);
        // lenient: not every stub here is exercised on every call path (e.g. role/status
        // checks short-circuit, or exceptions thrown before userId() is read).
        lenient().when(authPrincipal.userId()).thenReturn(USER_ID);
        lenient().when(authPrincipal.roles()).thenReturn(roles);
        lenient().when(authPrincipal.status()).thenReturn(status);
        return authPrincipal;
    }

    @Test
    @DisplayName("STOMP 헤더를 읽을 수 없는 메시지는 예외가 발생한다")
    void throwsWhenNoStompAccessor() {
        Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).build();

        assertThatThrownBy(() -> sut.preSend(message, channel))
                .isInstanceOf(InsufficientAuthenticationException.class)
                .hasMessageContaining("WEBSOCKET_NO_TOKEN");
    }

    @Nested
    @DisplayName("인증")
    class Authentication {

        @Test
        @DisplayName("Authorization 헤더가 없으면 예외가 발생한다")
        void throwsWhenNoAuthorizationHeader() {
            Message<byte[]> message = stompMessage(StompCommand.CONNECT, null, null);

            assertThatThrownBy(() -> sut.preSend(message, channel))
                    .isInstanceOf(InsufficientAuthenticationException.class)
                    .hasMessageContaining("WEBSOCKET_UNAUTHORIZED");
        }

        @Test
        @DisplayName("유효한 토큰으로 CONNECT하면 메시지에 인증 정보가 세팅된다")
        void setsAuthenticatedUserOnConnect() {
            AuthPrincipal authPrincipal = mockAuthPrincipal(List.of("USER"), "ACTIVE");
            when(jwtTokenParser.parseToken("Bearer token")).thenReturn(authPrincipal);
            Message<byte[]> message = stompMessage(StompCommand.CONNECT, "Bearer token", null);

            Message<?> result = sut.preSend(message, channel);

            StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
            assertThat(resultAccessor.getUser()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
            UsernamePasswordAuthenticationToken token =
                    (UsernamePasswordAuthenticationToken) resultAccessor.getUser();
            assertThat(token.getPrincipal()).isSameAs(authPrincipal);
            assertThat(token.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("USER");
        }

        @Test
        @DisplayName("USER 권한이 없으면 접근이 거부된다")
        void throwsWhenRoleMissing() {
            AuthPrincipal authPrincipal = mockAuthPrincipal(List.of("ADMIN"), "ACTIVE");
            when(jwtTokenParser.parseToken("Bearer token")).thenReturn(authPrincipal);
            Message<byte[]> message = stompMessage(StompCommand.CONNECT, "Bearer token", null);

            assertThatThrownBy(() -> sut.preSend(message, channel))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("WEBSOCKET_ACCESS_DENIED");
        }

        @Test
        @DisplayName("허용되지 않은 상태의 유저는 접근이 거부된다")
        void throwsWhenStatusNotAllowed() {
            AuthPrincipal authPrincipal = mockAuthPrincipal(List.of("USER"), "SUSPENDED");
            when(jwtTokenParser.parseToken("Bearer token")).thenReturn(authPrincipal);
            Message<byte[]> message = stompMessage(StompCommand.CONNECT, "Bearer token", null);

            assertThatThrownBy(() -> sut.preSend(message, channel))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("WEBSOCKET_ACCESS_DENIED");
        }
    }

    @Nested
    @DisplayName("SUBSCRIBE / SEND")
    class SubscribeAndSend {

        @Test
        @DisplayName("목적지가 채팅방 참여자 검증을 통과하면 메시지가 그대로 전달된다")
        void validatesParticipantForDestination() {
            AuthPrincipal authPrincipal = mockAuthPrincipal(List.of("USER"), "ACTIVE");
            when(jwtTokenParser.parseToken("Bearer token")).thenReturn(authPrincipal);
            Message<byte[]> message = stompMessage(StompCommand.SUBSCRIBE, "Bearer token", "/sub/room/42");

            Message<?> result = sut.preSend(message, channel);

            assertThat(result).isNotNull();
            verify(chatRoomService).validateChatParticipant(42L, USER_ID);
        }

        @Test
        @DisplayName("목적지가 없으면 예외가 발생한다")
        void throwsWhenNoDestination() {
            AuthPrincipal authPrincipal = mockAuthPrincipal(List.of("USER"), "ACTIVE");
            when(jwtTokenParser.parseToken("Bearer token")).thenReturn(authPrincipal);
            Message<byte[]> message = stompMessage(StompCommand.SEND, "Bearer token", null);

            assertThatThrownBy(() -> sut.preSend(message, channel))
                    .isInstanceOf(StompConversionException.class)
                    .hasMessageContaining("WEBSOCKET_NO_DESTINATION");

            verify(chatRoomService, never()).validateChatParticipant(any(), any());
        }

        @Test
        @DisplayName("목적지 마지막 세그먼트가 숫자가 아니면 예외가 발생한다")
        void throwsWhenDestinationIdIsInvalid() {
            AuthPrincipal authPrincipal = mockAuthPrincipal(List.of("USER"), "ACTIVE");
            when(jwtTokenParser.parseToken("Bearer token")).thenReturn(authPrincipal);
            Message<byte[]> message = stompMessage(StompCommand.SEND, "Bearer token", "/pub/room/abc");

            assertThatThrownBy(() -> sut.preSend(message, channel))
                    .isInstanceOf(StompConversionException.class)
                    .hasMessageContaining("WEBSOCKET_INVALID_DESTINATION");
        }
    }

    @Test
    @DisplayName("그 외 커맨드는 인증/인가 검증 없이 그대로 통과된다")
    void passesThroughOtherCommands() {
        Message<byte[]> message = stompMessage(StompCommand.DISCONNECT, null, null);

        Message<?> result = sut.preSend(message, channel);

        assertThat(result).isNotNull();
        verify(chatRoomService, never()).validateChatParticipant(any(), any());
    }
}
