package io.playground.chatservice.presentation;

import io.playground.chatservice.application.usecase.ChatMessageService;
import io.playground.chatservice.domain.ChatMessage;
import io.playground.securitycore.jwt.AuthPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    private ChatMessageController sut;

    private static final Long USER_ID = 1L;
    private static final Long CHAT_ROOM_ID = 10L;

    @BeforeEach
    void setUp() {
        sut = new ChatMessageController(chatMessageService);
    }

    @Test
    @DisplayName("인증된 사용자의 principal에서 userId를 꺼내 메시지 전송을 위임한다")
    void sendsChatMessage() {
        Authentication authentication = mock(Authentication.class);
        AuthPrincipal authPrincipal = mock(AuthPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(authPrincipal);
        when(authPrincipal.userId()).thenReturn(USER_ID);

        ChatRequestDto.SendChatMessage dto =
                new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "hi", null);
        when(chatMessageService.sendChatMessage(USER_ID, CHAT_ROOM_ID, dto)).thenReturn(100L);

        Long result = sut.sendChatMessage(authentication, CHAT_ROOM_ID, dto);

        assertThat(result).isEqualTo(100L);
        verify(chatMessageService).sendChatMessage(USER_ID, CHAT_ROOM_ID, dto);
    }

    @Test
    @DisplayName("메시지 목록 조회를 usecase에 위임하고 결과를 200 OK로 감싼다")
    void getsChatMessagesInfo() {
        AuthPrincipal authPrincipal = mock(AuthPrincipal.class);
        when(authPrincipal.userId()).thenReturn(USER_ID);

        ChatResponseDto.GetChatMessagesInfo expected = ChatResponseDto.GetChatMessagesInfo.builder()
                .lastReadMessageInfos(java.util.List.of())
                .chatMessageInfos(java.util.List.of())
                .nextCursor(null)
                .build();
        when(chatMessageService.getChatMessagesInfo(USER_ID, CHAT_ROOM_ID, 20, null)).thenReturn(expected);

        ResponseEntity<ChatResponseDto.GetChatMessagesInfo> response =
                sut.getChatMessagesInfo(authPrincipal, CHAT_ROOM_ID, 20, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(expected);
    }
}
