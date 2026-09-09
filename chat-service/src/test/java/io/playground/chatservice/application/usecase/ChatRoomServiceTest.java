package io.playground.chatservice.application.usecase;

import io.playground.chatservice.application.port.ChatParticipantPersistencePort;
import io.playground.chatservice.application.port.ChatRoomPersistencePort;
import io.playground.chatservice.application.port.UserViewQueryPort;
import io.playground.chatservice.domain.ChatParticipant;
import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.domain.UserView;
import io.playground.chatservice.exception.BusinessErrorCode;
import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.presentation.ChatRequestDto;
import io.playground.chatservice.presentation.ChatResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private UserViewQueryPort userViewQueryPort;

    @Mock
    private ChatRoomPersistencePort chatRoomPort;

    @Mock
    private ChatParticipantPersistencePort chatParticipantPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatRoomService sut;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        sut = new ChatRoomService(userViewQueryPort, chatRoomPort, chatParticipantPort, objectMapper);
    }

    private static UserView userView(Long userId, String nickname) {
        return UserView.of(userId, userId, nickname, Instant.now());
    }

    @Nested
    @DisplayName("createChatRoom")
    class CreateChatRoom {

        @Test
        @DisplayName("요청자가 participantIds에 없으면 자동으로 추가되고 관리자로 등록된다")
        void addsRequesterAsAdminWhenNotIncluded() {
            List<Long> participantIds = new ArrayList<>(List.of(2L, 3L));
            ChatRequestDto.CreateChatRoom dto =
                    new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.GROUP, participantIds, "room");

            when(userViewQueryPort.findAllByUserIds(anyList()))
                    .thenReturn(List.of(
                            userView(USER_ID, "me"),
                            userView(2L, "user2"),
                            userView(3L, "user3")
                    ));
            when(chatRoomPort.save(any(ChatRoom.class)))
                    .thenReturn(ChatRoom.of(55L, dto.type(), dto.name(), null));

            Long result = sut.createChatRoom(USER_ID, dto);

            assertThat(result).isEqualTo(55L);
            assertThat(dto.participantIds()).containsExactlyInAnyOrder(1L, 2L, 3L);

            ArgumentCaptor<List<ChatParticipant>> captor = ArgumentCaptor.forClass(List.class);
            verify(chatParticipantPort).saveAll(captor.capture());
            List<ChatParticipant> saved = captor.getValue();
            assertThat(saved).hasSize(3);
            assertThat(saved)
                    .filteredOn(participant -> participant.getParticipantId().equals(USER_ID))
                    .singleElement()
                    .satisfies(participant -> assertThat(participant.isAdmin()).isTrue());
            assertThat(saved)
                    .filteredOn(participant -> !participant.getParticipantId().equals(USER_ID))
                    .allSatisfy(participant -> assertThat(participant.isAdmin()).isFalse());
            assertThat(saved)
                    .allSatisfy(participant -> assertThat(participant.getChatRoomId()).isEqualTo(55L));
        }

        @Test
        @DisplayName("요청자가 이미 participantIds에 있으면 중복으로 추가되지 않는다")
        void doesNotDuplicateRequesterWhenAlreadyIncluded() {
            List<Long> participantIds = new ArrayList<>(List.of(USER_ID, 2L));
            ChatRequestDto.CreateChatRoom dto =
                    new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.PRIVATE, participantIds, null);

            when(userViewQueryPort.findAllByUserIds(anyList()))
                    .thenReturn(List.of(userView(USER_ID, "me"), userView(2L, "user2")));
            when(chatRoomPort.save(any(ChatRoom.class)))
                    .thenReturn(ChatRoom.of(56L, dto.type(), dto.name(), null));

            sut.createChatRoom(USER_ID, dto);

            assertThat(dto.participantIds()).hasSize(2);
        }

        @Test
        @DisplayName("일부 참여자를 찾을 수 없으면 채팅방을 생성하지 않고 예외가 발생한다")
        void throwsWhenSomeParticipantsNotFound() {
            List<Long> participantIds = new ArrayList<>(List.of(USER_ID, 2L, 3L));
            ChatRequestDto.CreateChatRoom dto =
                    new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.GROUP, participantIds, "room");

            when(userViewQueryPort.findAllByUserIds(anyList()))
                    .thenReturn(List.of(userView(USER_ID, "me"), userView(2L, "user2")));

            assertThatThrownBy(() -> sut.createChatRoom(USER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting(exception -> ((BusinessException) exception).getErrorCode())
                    .isEqualTo(BusinessErrorCode.PARTICIPANT_NOT_FOUND);

            verify(chatRoomPort, never()).save(any());
            verify(chatParticipantPort, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("getChatRoomInfos")
    class GetChatRoomInfos {

        @Test
        @DisplayName("채팅방별 참여자 목록을 묶어서 반환한다")
        void groupsParticipantsByChatRoom() {
            ChatRoom room1 = ChatRoom.of(1L, ChatRoom.RoomType.GROUP, "room1", Instant.now());
            ChatRoom room2 = ChatRoom.of(2L, ChatRoom.RoomType.PRIVATE, "room2", Instant.now());
            when(chatParticipantPort.findChatRoomsByParticipantId(USER_ID, 0, 10))
                    .thenReturn(List.of(room1, room2));

            ChatParticipant p1 = ChatParticipant.of(1L, 1L, USER_ID, "me", true, null);
            ChatParticipant p2 = ChatParticipant.of(2L, 1L, 2L, "user2", false, null);
            when(chatParticipantPort.findAllByChatRoomIds(List.of(1L, 2L)))
                    .thenReturn(List.of(p1, p2));

            List<ChatResponseDto.GetChatRoomInfo> result =
                    sut.getChatRoomInfos(USER_ID, 0, 10);

            assertThat(result).hasSize(2);
            ChatResponseDto.GetChatRoomInfo room1Info = result.stream()
                    .filter(info -> info.id().equals(1L))
                    .findFirst()
                    .orElseThrow();
            assertThat(room1Info.participantInfos()).hasSize(2);

            ChatResponseDto.GetChatRoomInfo room2Info = result.stream()
                    .filter(info -> info.id().equals(2L))
                    .findFirst()
                    .orElseThrow();
            assertThat(room2Info.participantInfos()).isEmpty();
        }
    }

    @Nested
    @DisplayName("validateChatParticipant")
    class ValidateChatParticipant {

        @Test
        @DisplayName("참여자이면 예외가 발생하지 않는다")
        void doesNotThrowWhenParticipant() {
            when(chatParticipantPort.existsByChatRoomIdAndParticipantId(1L, USER_ID))
                    .thenReturn(true);

            sut.validateChatParticipant(1L, USER_ID);
        }

        @Test
        @DisplayName("참여자가 아니면 예외가 발생한다")
        void throwsWhenNotParticipant() {
            when(chatParticipantPort.existsByChatRoomIdAndParticipantId(1L, USER_ID))
                    .thenReturn(false);

            assertThatThrownBy(() -> sut.validateChatParticipant(1L, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting(exception -> ((BusinessException) exception).getErrorCode())
                    .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);
        }
    }
}
