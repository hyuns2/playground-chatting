package io.playground.chatservice.presentation;

import io.playground.chatservice.application.usecase.ChatRoomService;
import io.playground.chatservice.domain.ChatRoom;
import io.playground.securitycore.jwt.AuthPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRoomControllerTest {

    @Mock
    private ChatRoomService chatRoomService;

    private ChatRoomController sut;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        sut = new ChatRoomController(chatRoomService);
    }

    @Test
    @DisplayName("인증된 사용자의 userId로 채팅방 생성을 위임하고 결과를 200 OK로 감싼다")
    void createsChatRoom() {
        AuthPrincipal authPrincipal = mock(AuthPrincipal.class);
        when(authPrincipal.userId()).thenReturn(USER_ID);

        ChatRequestDto.CreateChatRoom dto =
                new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(2L)), "room");
        when(chatRoomService.createChatRoom(USER_ID, dto)).thenReturn(55L);

        ResponseEntity<Long> response = sut.createChatRoom(authPrincipal, dto);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(55L);
        verify(chatRoomService).createChatRoom(USER_ID, dto);
    }

    @Test
    @DisplayName("인증된 사용자의 채팅방 목록 조회를 위임하고 결과를 200 OK로 감싼다")
    void getsChatRoomInfos() {
        AuthPrincipal authPrincipal = mock(AuthPrincipal.class);
        when(authPrincipal.userId()).thenReturn(USER_ID);

        List<ChatResponseDto.GetChatRoomInfo> expected = List.of();
        when(chatRoomService.getChatRoomInfos(USER_ID, 0, 10)).thenReturn(expected);

        ResponseEntity<List<ChatResponseDto.GetChatRoomInfo>> response =
                sut.getChatRoomInfos(authPrincipal, 0, 10);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(expected);
    }
}
