package io.playground.chatservice.application;

import io.playground.chatservice.application.port.ChatParticipantPersistencePort;
import io.playground.chatservice.application.port.ChatRoomPersistencePort;
import io.playground.chatservice.application.port.UserViewQueryPort;
import io.playground.chatservice.application.usecase.ChatRoomService;
import io.playground.chatservice.domain.ChatParticipant;
import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.domain.UserView;
import io.playground.chatservice.exception.BusinessErrorCode;
import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.presentation.ChatRequestDto;
import io.playground.chatservice.presentation.ChatResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {
  @Mock
  private UserViewQueryPort userViewQueryPort;
  @Mock
  private ChatRoomPersistencePort chatRoomPort;
  @Mock
  private ChatParticipantPersistencePort chatParticipantPort;
  @Spy
  private final ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks
  private ChatRoomService chatRoomService;

  private static final Long USER_ID = 1L;
  private static final Long CHAT_ROOM_ID = 365L;
  private static final Integer PAGE_NUM = 0;
  private static final Integer PAGE_SIZE = 25;

  @Nested
  @DisplayName("[채팅방 생성]")
  class CreateChatRoom {
    @Test
    @DisplayName("""
        1-1. 생성 요청자가 participantIds에 없으면
          - participantIds에 추가하고,
          - 관리자로 등록된다.
    """)
    void addsRequesterAsAdminWhenNotIncluded() {
      // given
      ChatRequestDto.CreateChatRoom dto = new ChatRequestDto.CreateChatRoom(
          ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(2L, 3L)), "room"
      );

      when(
          userViewQueryPort.findAllByUserIds(anyList())
      ).thenReturn(
          List.of(
              UserView.of(USER_ID, USER_ID, "me", Instant.now()),
              UserView.of(2L, 2L, "user2", Instant.now()),
              UserView.of(3L, 3L, "user3", Instant.now())
          )
      );
      when(
          chatRoomPort.save(any(ChatRoom.class))
      ).thenReturn(
          ChatRoom.of(CHAT_ROOM_ID, dto.type(), dto.name(), null)
      );

      // when
      Long result = chatRoomService.createChatRoom(USER_ID, dto);

      // then
      assertThat(result)
          .isEqualTo(CHAT_ROOM_ID);
      assertThat(dto.participantIds())
          .containsExactlyInAnyOrder(USER_ID, 2L, 3L);

      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<ChatParticipant>> captor = ArgumentCaptor.forClass(List.class);
      verify(chatParticipantPort)
          .saveAll(captor.capture());
      List<ChatParticipant> toSave = captor.getValue();
      assertThat(toSave)
          .hasSize(3);
      assertThat(toSave)
          .extracting(ChatParticipant::getNickname, ChatParticipant::isAdmin)
          .containsExactlyInAnyOrder(
              tuple("me", true),
              tuple("user2", false),
              tuple("user3", false)
          );
      assertThat(toSave)
          .allSatisfy(participant ->
              assertThat(participant.getChatRoomId())
                  .isEqualTo(CHAT_ROOM_ID)
          );
    }

    @Test
    @DisplayName("""
        1-2. 요청자가 이미 participantIds에 있으면
            - 중복으로 추가되지 않는다.
    """)
    void doesNotDuplicateRequesterWhenAlreadyIncluded() {
      // given
      ChatRequestDto.CreateChatRoom dto = new ChatRequestDto.CreateChatRoom(
          ChatRoom.RoomType.PRIVATE, new ArrayList<>(List.of(USER_ID, 2L)), null
      );

      when(
          userViewQueryPort.findAllByUserIds(anyList())
      ).thenReturn(
          List.of(
              UserView.of(USER_ID, USER_ID, "me", Instant.now()),
              UserView.of(2L, 2L, "user2", Instant.now())
          )
      );
      when(
          chatRoomPort.save(any(ChatRoom.class))
      ).thenReturn(
          ChatRoom.of(CHAT_ROOM_ID, dto.type(), dto.name(), null)
      );

      // when
      chatRoomService.createChatRoom(USER_ID, dto);

      // then
      assertThat(dto.participantIds())
          .hasSize(2);
    }

    @Test
    @DisplayName("""
        2. 일부 참여자를 찾을 수 없으면
          - 채팅방을 생성하지 않고,
          - 예외가 발생한다.
    """)
    void throwsWhenSomeParticipantsNotFound() {
      // given
      ChatRequestDto.CreateChatRoom dto = new ChatRequestDto.CreateChatRoom(
          ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(USER_ID, 2L, 3L)), "room"
      );

      when(
          userViewQueryPort.findAllByUserIds(anyList())
      ).thenReturn(
          List.of(
              UserView.of(USER_ID, USER_ID, "me", Instant.now()),
              UserView.of(2L, 2L, "user2", Instant.now())
          )
      );

      // when, then
      assertThatThrownBy(() ->
          chatRoomService.createChatRoom(USER_ID, dto)
      ).isInstanceOf(BusinessException.class)
          .extracting(exception -> ((BusinessException) exception).getErrorCode())
          .isEqualTo(BusinessErrorCode.PARTICIPANT_NOT_FOUND);

      verify(chatRoomPort, never())
          .save(any());
      verify(chatParticipantPort, never())
          .saveAll(any());
    }
  }

  @Nested
  @DisplayName("[채팅방 목록용 정보 조회]")
  class GetChatRoomInfos {
    @Test
    @DisplayName("""
        1. 채팅방별 참여자 목록을 묶어서 반환한다.
    """)
    void groupsParticipantsByChatRoom() {
      // given
      when(
          chatParticipantPort.findChatRoomsByParticipantId(
              USER_ID, PAGE_NUM, PAGE_SIZE
          )
      ).thenReturn(
          List.of(
              ChatRoom.of(CHAT_ROOM_ID, ChatRoom.RoomType.PRIVATE, "room1", Instant.now()),
              ChatRoom.of(CHAT_ROOM_ID + 1, ChatRoom.RoomType.GROUP, "room2", Instant.now())
          )
      );
      when(
          chatParticipantPort.findAllByChatRoomIds(
              List.of(CHAT_ROOM_ID, CHAT_ROOM_ID + 1)
          )
      ).thenReturn(
          List.of(
              ChatParticipant.of(USER_ID, CHAT_ROOM_ID, USER_ID, "me", true, null),
              ChatParticipant.of(USER_ID, CHAT_ROOM_ID + 1, USER_ID, "me", false, null),
              ChatParticipant.of(2L, CHAT_ROOM_ID + 1, 2L, "user2", false, null)
          )
      );

      // when
      List<ChatResponseDto.GetChatRoomInfo> result = chatRoomService
          .getChatRoomInfos(USER_ID, PAGE_NUM, PAGE_SIZE);

      // then
      assertThat(result)
          .hasSize(2);

      assertThat(
          result.stream()
              .filter(info ->
                  info.id()
                      .equals(CHAT_ROOM_ID)
              )
              .findFirst()
              .orElseThrow()
              .participantInfos()
      ).hasSize(1);
      assertThat(
          result.stream()
              .filter(info ->
                  info.id()
                      .equals(CHAT_ROOM_ID + 1)
              )
              .findFirst()
              .orElseThrow()
              .participantInfos()
      ).hasSize(2);
    }
  }

  @Nested
  @DisplayName("[채팅참여자 검증]")
  class ValidateChatParticipant {
    @Test
    @DisplayName("""
        1-1. 정상 참여자이면
          - 예외가 발생하지 않는다.
    """)
    void doesNotThrowWhenParticipant() {
      // given
      when(
          chatParticipantPort.existsByChatRoomIdAndParticipantId(CHAT_ROOM_ID, USER_ID)
      ).thenReturn(true);

      // when, then
      chatRoomService.validateChatParticipant(CHAT_ROOM_ID, USER_ID);
    }

    @Test
    @DisplayName("""
        1-2. 참여자가 아니면
          - 예외가 발생한다.
    """)
    void throwsWhenNotParticipant() {
      // given
      when(
          chatParticipantPort.existsByChatRoomIdAndParticipantId(CHAT_ROOM_ID, USER_ID)
      ).thenReturn(false);

      // when, then
      assertThatThrownBy(() ->
          chatRoomService.validateChatParticipant(CHAT_ROOM_ID, USER_ID)
      ).isInstanceOf(BusinessException.class)
          .extracting(exception -> ((BusinessException) exception).getErrorCode())
          .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);
    }
  }
}
