package io.playground.chatservice.infrastructure.websocket;

import io.playground.chatservice.application.usecase.ChatRoomService;
import io.playground.securitycore.jwt.AuthPrincipal;
import io.playground.securitycore.jwt.JwtTokenParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompConversionException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebsocketInterceptor implements ChannelInterceptor {
    private final ChatRoomService chatRoomService;
    private final JwtTokenParser jwtTokenParser;

    @Value("${auth.allowed-statuses}")
    private final List<String> allowedStatuses;

    /*
        ToDo: 유저 단위의 이벤트로 인해, 웹소켓 세션 직접 관리 고려
            1. 채팅방 퇴장 시, 모든 기기에서 UNSUBSCRIBE 필요
            2. 유저의 모든 기기 연결 해제 시, 모든 기기 DISCONNECT 필요
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor == null)
            throw new InsufficientAuthenticationException(
                    "WEBSOCKET_NO_TOKEN"
            );

        switch (accessor.getCommand()) {
            case CONNECT -> {
                AuthPrincipal authPrincipal = authenticate(accessor);
                authorizeAndSet(authPrincipal, accessor);
            }
            case SUBSCRIBE, SEND -> {
                AuthPrincipal authPrincipal = authenticate(accessor);
                authorizeAndSet(authPrincipal, accessor);

                if (accessor.getDestination() == null)
                    throw new StompConversionException(
                            "WEBSOCKET_NO_DESTINATION"
                    );
                chatRoomService.validateChatParticipant(
                        extractChatRoomId(
                                accessor.getDestination()
                        ),
                        authPrincipal.userId()
                );
            }
            case null -> throw new StompConversionException(
                    "WEBSOCKET_NO_COMMAND"
            );
            default -> {}
        }

        return message;
    }

    private AuthPrincipal authenticate(StompHeaderAccessor accessor) {
        if (
                !accessor.containsNativeHeader(
                        "Authorization"
                )
        )
            throw new InsufficientAuthenticationException(
                    "WEBSOCKET_UNAUTHORIZED"
            );

        return jwtTokenParser.parseToken(
                accessor.getFirstNativeHeader(
                        "Authorization"
                )
        );
    }

    private void authorizeAndSet(AuthPrincipal authPrincipal,
                                 StompHeaderAccessor accessor) {
        if (
                !authPrincipal.roles()
                        .contains("USER") ||
                !allowedStatuses.contains(
                        authPrincipal.status()
                )
        )
            throw new AccessDeniedException(
                    "WEBSOCKET_ACCESS_DENIED"
            );

        accessor.setUser(
                new UsernamePasswordAuthenticationToken(
                        authPrincipal,
                        null,
                        authPrincipal.roles().stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList()
                )
        );
    }

    private Long extractChatRoomId(String destination) {
        try {
            String[] segments = destination.split("/");

            return Long.parseLong(
                    segments[segments.length - 1]
            );
        } catch (Exception e) {
            throw new StompConversionException(
                    "WEBSOCKET_INVALID_DESTINATION"
            );
        }
    }
}
