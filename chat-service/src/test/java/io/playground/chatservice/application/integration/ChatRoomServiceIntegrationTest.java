package io.playground.chatservice.application.integration;

import io.playground.chatservice.application.usecase.ChatMessageService;
import io.playground.chatservice.application.usecase.ChatRoomService;
import io.playground.chatservice.common.BaseIntegrationTest;
import io.playground.chatservice.domain.ChatMessage;
import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.exception.BusinessErrorCode;
import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.infrastructure.jpa.entity.UserViewEntity;
import io.playground.chatservice.infrastructure.jpa.repository.ChatMessageRepository;
import io.playground.chatservice.infrastructure.jpa.repository.ChatParticipantRepository;
import io.playground.chatservice.infrastructure.jpa.repository.ChatRoomRepository;
import io.playground.chatservice.infrastructure.jpa.repository.UserViewRepository;
import io.playground.chatservice.presentation.ChatRequestDto;
import io.playground.chatservice.presentation.ChatResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@BaseIntegrationTest
public class ChatRoomServiceIntegrationTest {
  @Autowired
  private ChatRoomService chatRoomService;

  @Autowired
  private UserViewRepository userViewRepository;
  @Autowired
  private ChatRoomRepository chatRoomRepository;
  @Autowired
  private ChatParticipantRepository chatParticipantRepository;
  @Autowired
  private ChatMessageRepository chatMessageRepository;

  @Autowired
  private ChatMessageService chatMessageService;

  private static final Long USER_ID = 1L;

  @BeforeEach
  void beforeEach() {
    chatMessageRepository.deleteAll();
    chatParticipantRepository.deleteAll();
    chatRoomRepository.deleteAll();
    userViewRepository.deleteAll();

    userViewRepository.saveAll(
        List.of(
            new UserViewEntity(null, USER_ID, "me", Instant.now()),
            new UserViewEntity(null, 2L, "user2", Instant.now()),
            new UserViewEntity(null, 3L, "user3", Instant.now())
        )
    );
  }

  @Nested
  @DisplayName("[채팅방 생성]")
  class CreateChatRoom {
    @Test
    @DisplayName("""
        1-1. 채팅방 생성 요청 시
          - 채팅방과 참여자가 생성되고,
          - 생성 요청자는 관리자가 된다.
    """)
    void createsChatRoomWithParticipants() {
      // given
      ChatRequestDto.CreateChatRoom dto = new ChatRequestDto.CreateChatRoom(
          ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(2L, 3L)), "room"
      );

      // when
      Long chatRoomId = chatRoomService.createChatRoom(USER_ID, dto);

      // then
      assertThat(chatRoomRepository.findById(chatRoomId))
          .isPresent();
      assertThat(chatParticipantRepository.findAllByChatRoom_IdIn(List.of(chatRoomId)))
          .hasSize(3)
          .anySatisfy(participant -> {
            assertThat(participant.getParticipantId())
                .isEqualTo(USER_ID);
            assertThat(participant.isAdmin())
                .isTrue();
          });
    }

    @Test
    @DisplayName("""
        1-2. not found userId가 포함되면
          - 채팅방을 생성하지 않고,
          - 예외가 발생한다.
    """)
    void throwsWhenParticipantNotFound() {
      // given
      ChatRequestDto.CreateChatRoom dto = new ChatRequestDto.CreateChatRoom(
          ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(2L, 999L)), "room"
      );

      // when, then
      assertThatThrownBy(() -> chatRoomService.createChatRoom(USER_ID, dto))
          .isInstanceOf(BusinessException.class)
          .extracting(exception -> ((BusinessException) exception).getErrorCode())
          .isEqualTo(BusinessErrorCode.PARTICIPANT_NOT_FOUND);

      assertThat(chatRoomRepository.count())
          .isZero();
    }
  }

  @Nested
  @DisplayName("[채팅방 조회]")
  class GetChatRoomInfos {
    @Test
    @DisplayName("""
        2-1. 자신의 채팅방 목록을 요청하면
          - 채팅방 정보와 (타입, 이름, 마지막 메시지 시간 등)
          - 참여자 정보가 함께 반환된다. (참여자 id, 닉네임, 관리자 여부 등)
    """)
    void getsChatRoomInfosWithParticipants() {
      // given
      Long roomId = chatRoomService.createChatRoom(
          USER_ID,
          new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.PRIVATE, new ArrayList<>(List.of(2L)), "room")
      );

      // when
      List<ChatResponseDto.GetChatRoomInfo> result = chatRoomService.getChatRoomInfos(USER_ID, 0, 10);

      // then
      assertThat(result)
          .hasSize(1);
      assertThat(result.getFirst().id())
          .isEqualTo(roomId);
      assertThat(result.getFirst().participantInfos())
          .extracting(ChatResponseDto.ParticipantInfo::id)
          .containsExactlyInAnyOrder(USER_ID, 2L);
    }

    @Test
    @DisplayName("""
        2-2. 아무 채팅방에도 참여하지 않은 사용자가 채팅방 목록을 요청하면
          - 빈 목록이 반환된다.
    """)
    void returnsEmptyListForNonParticipant() {
      // given
      chatRoomService.createChatRoom(
          USER_ID,
          new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.PRIVATE, new ArrayList<>(List.of(2L)), "room")
      );

      // when
      List<ChatResponseDto.GetChatRoomInfo> result = chatRoomService.getChatRoomInfos(3L, 0, 10);

      // then
      assertThat(result)
          .isEmpty();
    }

    @Test
    @DisplayName("""
        3. 채팅방 목록은
          - 마지막 메시지 전송 시간 기준으로
          - 내림차순 정렬된다.
    """)
    void getsChatRoomInfosOrderByLastMessagedAt() {
      // given
      Long roomId1 = chatRoomService.createChatRoom(
          USER_ID,
          new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.PRIVATE, new ArrayList<>(List.of(2L)), "room1")
      );
      chatMessageService.sendChatMessage(
          USER_ID,
          roomId1,
          new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "First", null)
      );

      Long roomId2 = chatRoomService.createChatRoom(
          USER_ID,
          new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.PRIVATE, new ArrayList<>(List.of(3L)), "room2")
      );
      chatMessageService.sendChatMessage(
          3L,
          roomId2,
          new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "Second", null)
      );

      Long roomId3 = chatRoomService.createChatRoom(
          USER_ID,
          new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(2L, 3L)), "room3")
      );
      chatMessageService.sendChatMessage(
          2L,
          roomId3,
          new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "Third", null)
      );

      // when
      List<ChatResponseDto.GetChatRoomInfo> result = chatRoomService.getChatRoomInfos(USER_ID, 0, 10);

      // then
      assertThat(result)
          .hasSize(3);

      assertThat(result.getFirst().id())
          .isEqualTo(roomId3);
      assertThat(result.getFirst().participantInfos())
          .extracting(ChatResponseDto.ParticipantInfo::id)
          .containsExactlyInAnyOrder(USER_ID, 2L, 3L);

      assertThat(result.getLast().id())
          .isEqualTo(roomId1);
      assertThat(result.getLast().participantInfos())
          .extracting(ChatResponseDto.ParticipantInfo::id)
          .containsExactlyInAnyOrder(USER_ID, 2L);
    }
  }

  @Nested
  @DisplayName("[채팅참여자 검증]")
  class ValidateChatParticipants {
    @Test
    @DisplayName("""
        4-1. 채팅방 참여자이면
          - 예외가 발생하지 않는다.
    """)
    void doesNotThrowWhenParticipant() {
      // given
      Long roomId = chatRoomService.createChatRoom(
          USER_ID,
          new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.PRIVATE, new ArrayList<>(List.of(2L)), "room")
      );

      // when, then
      chatRoomService.validateChatParticipant(roomId, USER_ID);
    }

    @Test
    @DisplayName("""
        4-2. 채팅방 참여자가 아니면
          - 예외가 발생한다.
    """)
    void throwsWhenNotParticipant() {
      // given
      Long roomId = chatRoomService.createChatRoom(
          USER_ID,
          new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.PRIVATE, new ArrayList<>(List.of(2L)), "room")
      );

      // when, then
      assertThatThrownBy(() -> chatRoomService.validateChatParticipant(roomId, 3L))
          .isInstanceOf(BusinessException.class)
          .extracting(exception -> ((BusinessException) exception).getErrorCode())
          .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);
    }
  }
}
