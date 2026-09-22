package io.playground.chatservice.application.integration;

import io.playground.chatservice.application.event.ChatEventDto;
import io.playground.chatservice.application.usecase.ChatMessageService;
import io.playground.chatservice.application.usecase.ChatRoomService;
import io.playground.chatservice.common.BaseIntegrationTest;
import io.playground.chatservice.domain.ChatMessage;
import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.exception.BusinessErrorCode;
import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.infrastructure.jpa.entity.ChatMessageEntity;
import io.playground.chatservice.infrastructure.jpa.entity.ChatParticipantEntity;
import io.playground.chatservice.infrastructure.jpa.entity.ChatRoomEntity;
import io.playground.chatservice.infrastructure.jpa.entity.UserViewEntity;
import io.playground.chatservice.infrastructure.jpa.repository.ChatMessageRepository;
import io.playground.chatservice.infrastructure.jpa.repository.ChatParticipantRepository;
import io.playground.chatservice.infrastructure.jpa.repository.ChatRoomRepository;
import io.playground.chatservice.infrastructure.jpa.repository.UserViewRepository;
import io.playground.chatservice.infrastructure.outbox.OutboxRepository;
import io.playground.chatservice.presentation.ChatRequestDto;
import io.playground.chatservice.presentation.ChatResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@BaseIntegrationTest
public class ChatMessageServiceIntegrationTest {
  @Autowired
  private ChatMessageService chatMessageService;

  @Autowired
  private UserViewRepository userViewRepository;
  @Autowired
  private ChatRoomRepository chatRoomRepository;
  @Autowired
  private ChatParticipantRepository chatParticipantRepository;
  @Autowired
  private ChatMessageRepository chatMessageRepository;
  @Autowired
  private OutboxRepository outboxRepository;

  @Autowired
  private ChatRoomService chatRoomService;
  @Autowired
  private ObjectMapper objectMapper;

  private static final Long SENDER_ID = 1L;
  private static final Long THE_OTHER_ID = 2L;
  private Long chatRoomId;

  @BeforeEach
  void beforeEach() {
    outboxRepository.deleteAll();
    chatMessageRepository.deleteAll();
    chatParticipantRepository.deleteAll();
    chatRoomRepository.deleteAll();
    userViewRepository.deleteAll();

    userViewRepository.saveAll(
        List.of(
            new UserViewEntity(null, SENDER_ID, "me", Instant.now()),
            new UserViewEntity(null, THE_OTHER_ID, "user2", Instant.now()),
            new UserViewEntity(null, 3L, "user3", Instant.now())
        )
    );

    chatRoomId = chatRoomService.createChatRoom(
        SENDER_ID,
        new ChatRequestDto.CreateChatRoom(
            ChatRoom.RoomType.PRIVATE, new ArrayList<>(List.of(THE_OTHER_ID)), "room"
        )
    );
  }

  @Nested
  @DisplayName("[채팅메시지 발송]")
  class SendChatMessage {
    @Test
    @DisplayName("""
        1-1. 정상 참여자가 메시지를 보내면
          - DB에 저장되고,
          - 채팅방 최종 발신 시각이 갱신되며,
          - 이벤트가 발행된다.
    """)
    void sendsMessageAndPersistsIt() {
      // given
      ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
          ChatMessage.MessageType.TEXT, "hello", null
      );

      // when
      Long messageId = chatMessageService.sendChatMessage(SENDER_ID, chatRoomId, dto);

      // then
      ChatMessageEntity saved = chatMessageRepository.findById(messageId)
          .orElseThrow();
      assertThat(saved.getContent())
          .isEqualTo("hello");
      assertThat(saved.getSenderId())
          .isEqualTo(SENDER_ID);
      assertThat(saved.getChatRoom().getId())
          .isEqualTo(chatRoomId);

      ChatRoomEntity updatedRoom = chatRoomRepository.findById(chatRoomId)
          .orElseThrow();
      assertThat(updatedRoom.getLastMessagedAt())
          .isNotNull();

      assertThat(outboxRepository.findAll())
          .singleElement()
          .satisfies(outbox -> {
            assertThat(outbox.getEventType())
                .isEqualTo(ChatEventDto.EventType.CHAT_MESSAGE_SENT);
            assertThat(Long.parseLong(outbox.getPartitionKey()))
                .isEqualTo(chatRoomId);
            assertThat(
                objectMapper.readValue(
                    outbox.getPayload(), ChatEventDto.ChatMessageSent.class
                ).content()
            ).isEqualTo("hello");
          });
    }

    @Test
    @DisplayName("""
        1-2. 채팅방 참여자가 아닌 유저가 메시지를 보내면
          - 아무것도 저장되지 않고,
          - 예외가 발생한다.
    """)
    void throwsWhenSenderIsNotParticipant() {
      // given
      ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
          ChatMessage.MessageType.TEXT, "hello", null
      );

      // when, then
      assertThatThrownBy(() -> chatMessageService.sendChatMessage(999L, chatRoomId, dto))
          .isInstanceOf(BusinessException.class)
          .extracting(exception -> ((BusinessException) exception).getErrorCode())
          .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);

      assertThat(chatMessageRepository.count())
          .isZero();
      assertThat(outboxRepository.count())
          .isZero();

      ChatRoomEntity chatRoom = chatRoomRepository.findById(chatRoomId)
          .orElseThrow();
      assertThat(chatRoom.getLastMessagedAt())
          .isNull();
    }

    @Test
    @DisplayName("""
        2-1. parentMessageId가 같은 채팅방의 메시지를 가리키면
          - 부모 메시지 id와 함께 저장된다.
    """)
    void sendsMessageWithParent() {
      // given
      Long parentId = chatMessageService.sendChatMessage(
          THE_OTHER_ID,
          chatRoomId,
          new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "parent", null)
      );

      // when
      Long childId = chatMessageService.sendChatMessage(
          SENDER_ID,
          chatRoomId,
          new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "child", parentId)
      );

      // then
      ChatMessageEntity child = chatMessageRepository.findById(childId)
          .orElseThrow();
      assertThat(child.getParentMessage().getId())
          .isEqualTo(parentId);
    }

    @Test
    @DisplayName("""
        2-2. parentMessageId가 존재하지 않으면
          - 채팅 메시지가 저장되지 않고,
          - 예외가 발생한다.
    """)
    void throwsWhenParentMessageDoesNotExist() {
      // given
      ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
          ChatMessage.MessageType.TEXT, "hello", 999L
      );

      // when, then
      assertThatThrownBy(() -> chatMessageService.sendChatMessage(SENDER_ID, chatRoomId, dto))
          .isInstanceOf(BusinessException.class)
          .extracting(exception -> ((BusinessException) exception).getErrorCode())
          .isEqualTo(BusinessErrorCode.INVALID_PARENT_MESSAGE);

      assertThat(chatMessageRepository.count())
          .isZero();
    }

    @Test
    @DisplayName("""
        2-3. parentMessageId가 해당 채팅방의 메시지를 가리키지 않으면
          - 채팅 메시지가 저장되지 않고,
          - 예외가 발생한다.
    """)
    void throwsWhenParentMessageIsNotInSameChatRoom() {
      // given
      Long otherRoomId = chatRoomService.createChatRoom(
          SENDER_ID,
          new ChatRequestDto.CreateChatRoom(
              ChatRoom.RoomType.PRIVATE, new ArrayList<>(List.of(3L)), "other room"
          )
      );
      Long parentMessageIdInOtherRoom = chatMessageService.sendChatMessage(
          SENDER_ID,
          otherRoomId,
          new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "parent", null)
      );

      ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
          ChatMessage.MessageType.TEXT, "hello", parentMessageIdInOtherRoom
      );

      // when, then
      assertThatThrownBy(() -> chatMessageService.sendChatMessage(SENDER_ID, chatRoomId, dto))
          .isInstanceOf(BusinessException.class)
          .extracting(exception -> ((BusinessException) exception).getErrorCode())
          .isEqualTo(BusinessErrorCode.INVALID_PARENT_MESSAGE);

      assertThat(chatMessageRepository.count())
          .isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("[채팅메시지 조회]")
  class GetChatMessages {
    @Test
    @DisplayName("""
        3. 메시지를 조회하면
          - 최신 순으로 반환되고,
          - 조회자의 마지막으로 읽은 메시지가 갱신된다.
    """)
    void getsChatMessagesInfoAndUpdatesLastRead() {
      // given
      Long firstMessageId = chatMessageService.sendChatMessage(
          THE_OTHER_ID,
          chatRoomId,
          new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "hello1", null)
      );
      Long secondMessageId = chatMessageService.sendChatMessage(
          THE_OTHER_ID,
          chatRoomId,
          new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "hello2", null)
      );

      // when
      ChatResponseDto.GetChatMessagesInfo result = chatMessageService.getChatMessagesInfo(
          SENDER_ID, chatRoomId, 10, null
      );

      // then
      assertThat(result.chatMessageInfos())
          .extracting(ChatResponseDto.ChatMessageInfo::id)
          .containsExactly(secondMessageId, firstMessageId);
      assertThat(result.lastReadMessageInfos()).hasSize(2);

      List<ChatParticipantEntity> participants = chatParticipantRepository.findAllByChatRoom_IdIn(
          List.of(chatRoomId)
      );
      assertThat(participants)
          .extracting(ChatParticipantEntity::getLastReadMessageId)
          .containsOnlyOnce(secondMessageId);
    }

    @Test
    @DisplayName("""
        4. 채팅방에 메시지가 없으면
          - 다음 커서는 null이고,
          - 마지막으로 읽은 메시지는 갱신되지 않는다.
    """)
    void returnsNullCursorWhenNoMessages() {
      // when
      ChatResponseDto.GetChatMessagesInfo result = chatMessageService.getChatMessagesInfo(
          SENDER_ID, chatRoomId, 10, null
      );

      // then
      assertThat(result.chatMessageInfos()).isEmpty();
      assertThat(result.nextCursor()).isNull();

      List<ChatParticipantEntity> participants = chatParticipantRepository.findAllByChatRoom_IdIn(
          List.of(chatRoomId)
      );
      assertThat(participants)
          .extracting(ChatParticipantEntity::getLastReadMessageId)
          .containsOnlyNulls();
    }

    @Test
    @DisplayName("""
        5. 채팅방 참여자가 아닌 유저가 메시지 목록을 조회하면
          - 예외가 발생한다.
    """)
    void throwsWhenViewerIsNotParticipant() {
      // when, then
      assertThatThrownBy(() -> chatMessageService.getChatMessagesInfo(999L, chatRoomId, 10, null))
          .isInstanceOf(BusinessException.class)
          .extracting(exception -> ((BusinessException) exception).getErrorCode())
          .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);
    }

    @Test
    @DisplayName("""
        6. cursor가 주어지면
          - cursor 이후의 메시지만
          - size만큼 최신 순으로 반환된다.
    """)
    void limitsMessagesBySize() {
      // given
      for (int i = 0; i < 5; i++)
        chatMessageService.sendChatMessage(
            THE_OTHER_ID,
            chatRoomId,
            new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "hello" + i, null)
        );
      ChatMessageEntity forCursor = chatMessageRepository.findAll().stream()
          .filter(message -> message.getContent().equals("hello2"))
          .findFirst()
          .orElseThrow();

      // when
      ChatResponseDto.GetChatMessagesInfo result = chatMessageService.getChatMessagesInfo(
          SENDER_ID,
          chatRoomId,
          3,
          new ChatRequestDto.ChatMessageCursor(
              forCursor.getId(),
              LocalDateTime.ofInstant(
                forCursor.getCreatedAt(),
                ZoneId.systemDefault()
              )
          )
      );

      // then
      assertThat(result.chatMessageInfos())
          .hasSize(2);
      assertThat(result.chatMessageInfos())
          .extracting(ChatResponseDto.ChatMessageInfo::content)
          .containsExactly("hello1", "hello0");

      assertThat(result.nextCursor().id())
          .isEqualTo(result.chatMessageInfos().getLast().id());
      assertThat(result.nextCursor().createdAt())
          .isEqualTo(result.chatMessageInfos().getLast().createdAt());
    }
  }
}
