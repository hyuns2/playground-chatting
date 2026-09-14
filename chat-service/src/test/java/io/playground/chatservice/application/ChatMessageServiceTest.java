package io.playground.chatservice.application;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.application.event.ChatEventDto;
import io.playground.chatservice.application.event.EventProducerPort;
import io.playground.chatservice.application.port.ChatMessagePersistencePort;
import io.playground.chatservice.application.port.ChatParticipantPersistencePort;
import io.playground.chatservice.application.port.ChatRoomPersistencePort;
import io.playground.chatservice.application.usecase.ChatMessageService;
import io.playground.chatservice.domain.ChatMessage;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {
  @Mock
  private ChatRoomPersistencePort chatRoomPort;
  @Mock
  private ChatParticipantPersistencePort chatParticipantPort;
  @Mock
  private ChatMessagePersistencePort chatMessagePort;
  @Mock
  private EventProducerPort eventProducerPort;

  @InjectMocks
  private ChatMessageService chatMessageService;

  private static final Long USER_ID = 1L;
  private static final Long CHAT_ROOM_ID = 365L;
  private static final Long CHAT_MESSAGE_ID = 999L;
  private static final Long PARENT_MESSAGE_ID = 100L;
  private static final Integer PAGE_SIZE = 25;

  @Nested
  @DisplayName("[채팅메시지 발송]")
  class SendChatMessage {
    @Test
    @DisplayName("""
        1. 정상 참여자가 메시지를 보내면
          - 메시지가 저장되고,
          - 채팅방 최종 발신 시각이 갱신되며,
          - 이벤트가 발행된다.
    """)
    void sendsMessageAndUpdatesRoomAndProducesEvent() {
      // given
      ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
          ChatMessage.MessageType.TEXT, "hello", null
      );
      Instant createdAt = Instant.now();

      when(
          chatParticipantPort.existsByChatRoomIdAndParticipantId(CHAT_ROOM_ID, USER_ID)
      ).thenReturn(true);
      when(
          chatMessagePort.save(any(ChatMessage.class))
      ).thenReturn(
          ChatMessage.of(
              CHAT_MESSAGE_ID, CHAT_ROOM_ID, USER_ID, dto.type(), dto.content(), null, createdAt
          )
      );

      // when
      Long result = chatMessageService.sendChatMessage(USER_ID, CHAT_ROOM_ID, dto);

      // then
      assertThat(result)
          .isEqualTo(CHAT_MESSAGE_ID);

      ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
      verify(chatMessagePort)
          .save(messageCaptor.capture());
      ChatMessage toSave = messageCaptor.getValue();
      assertThat(toSave.getChatRoomId()).isEqualTo(CHAT_ROOM_ID);
      assertThat(toSave.getSenderId()).isEqualTo(USER_ID);
      assertThat(toSave.getType()).isEqualTo(dto.type());
      assertThat(toSave.getContent()).isEqualTo(dto.content());
      assertThat(toSave.getParentMessageId()).isNull();

      verify(chatRoomPort)
          .updateLastMessagedAtByChatRoomId(CHAT_ROOM_ID, createdAt);

      ArgumentCaptor<ChatEventDto.ChatMessageSent> payloadCaptor = ArgumentCaptor.forClass(ChatEventDto.ChatMessageSent.class);
      verify(eventProducerPort)
          .produce(
              anyString(),
              eq(ChatEventDto.EventType.CHAT_MESSAGE_SENT),
              any(Instant.class),
              payloadCaptor.capture()
          );
      assertThat(
          payloadCaptor.getValue().chatMessageId()
      ).isEqualTo(CHAT_MESSAGE_ID);
      assertThat(
          payloadCaptor.getValue().chatRoomId()
      ).isEqualTo(CHAT_ROOM_ID);

      verify(chatMessagePort, never())
          .existsByIdAndChatRoomId(any(), any());
    }

    @Test
    @DisplayName("""
        2-1. parentMessageId가 같은 채팅방에 존재하면
          - 정상적으로 메시지가 저장된다.
    """)
    void sendsMessageWithValidParentMessage() {
      // given
      ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
          ChatMessage.MessageType.TEXT, "reply", PARENT_MESSAGE_ID
      );

      when(
          chatParticipantPort.existsByChatRoomIdAndParticipantId(CHAT_ROOM_ID, USER_ID)
      ).thenReturn(true);
      when(
          chatMessagePort.existsByIdAndChatRoomId(dto.parentMessageId(), CHAT_ROOM_ID)
      ).thenReturn(true);
      when(
          chatMessagePort.save(any(ChatMessage.class))
      ).thenReturn(
          ChatMessage.of(
              CHAT_MESSAGE_ID, CHAT_ROOM_ID, USER_ID, dto.type(), dto.content(), dto.parentMessageId(), Instant.now()
          )
      );

      // when
      Long result = chatMessageService.sendChatMessage(USER_ID, CHAT_ROOM_ID, dto);

      // then
      assertThat(result)
          .isEqualTo(CHAT_MESSAGE_ID);

      verify(chatMessagePort)
          .existsByIdAndChatRoomId(dto.parentMessageId(), CHAT_ROOM_ID);
    }

    @Test
    @DisplayName("""
        2-2. parentMessageId가 같은 채팅방에 존재하지 않으면
          - 예외가 발생한다.
    """)
    void throwsWhenParentMessageIsInvalid() {
      // given
      ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
          ChatMessage.MessageType.TEXT, "reply", PARENT_MESSAGE_ID
      );

      when(
          chatParticipantPort.existsByChatRoomIdAndParticipantId(CHAT_ROOM_ID, USER_ID)
      ).thenReturn(true);
      when(
          chatMessagePort.existsByIdAndChatRoomId(dto.parentMessageId(), CHAT_ROOM_ID)
      ).thenReturn(false);

      // when, then
      assertThatThrownBy(() ->
          chatMessageService.sendChatMessage(USER_ID, CHAT_ROOM_ID, dto)
      ).isInstanceOf(BusinessException.class)
          .extracting(exception -> ((BusinessException) exception).getErrorCode())
          .isEqualTo(BusinessErrorCode.INVALID_PARENT_MESSAGE);

      verify(chatMessagePort, never())
          .save(any());
      verifyNoInteractions(eventProducerPort);
    }

    @Test
    @DisplayName("""
        3. 발송자가 채팅방 참여자가 아니면
          - 예외가 발생하고,
          - 이후 로직은 수행되지 않는다.
    """)
    void throwsWhenSenderIsNotParticipant() {
      // given
      ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
          ChatMessage.MessageType.TEXT, "hello", null
      );

      when(
          chatParticipantPort.existsByChatRoomIdAndParticipantId(CHAT_ROOM_ID, USER_ID)
      ).thenReturn(false);

      // when, then
      assertThatThrownBy(() ->
          chatMessageService.sendChatMessage(USER_ID, CHAT_ROOM_ID, dto)
      ).isInstanceOf(BusinessException.class)
          .extracting(exception -> ((BusinessException) exception).getErrorCode())
          .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);

      verify(chatMessagePort, never())
          .save(any());
      verify(chatRoomPort, never())
          .updateLastMessagedAtByChatRoomId(any(), any());
      verifyNoInteractions(eventProducerPort);
    }
  }

  @Nested
  @DisplayName("[채팅메시지 조회]")
  class GetChatMessagesInfo {
    @Test
    @DisplayName("""
        1. 아직 안 읽은 메시지가 있으면
          - 최신 메시지 id로 마지막으로 읽은 메시지가 갱신된다.
    """)
    void updatesLastReadMessageWhenNewMessageExists() {
      // given
      when(
          chatParticipantPort.findLastReadMessageInfosByChatRoomId(CHAT_ROOM_ID)
      ).thenReturn(
          List.of(
              new ChatQueryDto.LastReadMessageInfo(
                  USER_ID, USER_ID, null
              ),
              new ChatQueryDto.LastReadMessageInfo(
                  2L, 2L, CHAT_MESSAGE_ID
              )
          )
      );
      when(
          chatMessagePort.findAllByChatRoomIdAndCursor(eq(CHAT_ROOM_ID), eq(PAGE_SIZE), any())
      ).thenReturn(
          List.of(
              ChatMessage.of(
                  CHAT_MESSAGE_ID, CHAT_ROOM_ID, 2L, ChatMessage.MessageType.TEXT, "hi", null, Instant.now()
              ),
              ChatMessage.of(
                  CHAT_MESSAGE_ID - 1, CHAT_ROOM_ID, 2L, ChatMessage.MessageType.TEXT, "hello", null, Instant.now().minusSeconds(10)
              )
          )
      );

      // when
      ChatResponseDto.GetChatMessagesInfo result = chatMessageService
          .getChatMessagesInfo(USER_ID, CHAT_ROOM_ID, PAGE_SIZE, null);

      // then
      assertThat(result.chatMessageInfos())
          .hasSize(2);
      assertThat(
          result.chatMessageInfos()
              .getFirst()
              .id()
      ).isEqualTo(CHAT_MESSAGE_ID);
      assertThat(result.nextCursor().id())
          .isEqualTo(CHAT_MESSAGE_ID - 1);
      assertThat(result.lastReadMessageInfos())
          .hasSize(2);

      verify(chatParticipantPort)
          .updateLastReadMessageIdById(USER_ID, CHAT_MESSAGE_ID);
    }

    @Test
    @DisplayName("""
        2. 조회 요청자가 채팅방 참여자가 아니면
          - 예외가 발생한다.
    """)
    void throwsWhenRequesterIsNotParticipant() {
      // given
      when(
          chatParticipantPort.findLastReadMessageInfosByChatRoomId(CHAT_ROOM_ID)
      ).thenReturn(
          List.of(
              new ChatQueryDto.LastReadMessageInfo(2L, 2L, null),
              new ChatQueryDto.LastReadMessageInfo(3L, 3L, null)
          )
      );

      // when, then
      assertThatThrownBy(() ->
          chatMessageService.getChatMessagesInfo(USER_ID, CHAT_ROOM_ID, PAGE_SIZE, null)
      ).isInstanceOf(BusinessException.class)
          .extracting(exception -> ((BusinessException) exception).getErrorCode())
          .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);

      verify(chatMessagePort, never())
          .findAllByChatRoomIdAndCursor(any(), anyInt(), any());
    }

    @Test
    @DisplayName("""
        3. 조회된 메시지가 없으면
          - 다음 커서는 null이고,
          - 마지막으로 읽은 메시지는 갱신되지 않는다.
    """)
    void doesNotUpdateLastReadWhenNoMessages() {
      // given
      when(
          chatParticipantPort.findLastReadMessageInfosByChatRoomId(CHAT_ROOM_ID)
      ).thenReturn(
          List.of(
              new ChatQueryDto.LastReadMessageInfo(USER_ID, USER_ID, null)
          )
      );
      when(
          chatMessagePort.findAllByChatRoomIdAndCursor(eq(CHAT_ROOM_ID), eq(PAGE_SIZE), any())
      ).thenReturn(List.of());

      // when
      ChatResponseDto.GetChatMessagesInfo result = chatMessageService
          .getChatMessagesInfo(USER_ID, CHAT_ROOM_ID, PAGE_SIZE, null);

      // then
      assertThat(result.chatMessageInfos())
          .isEmpty();
      assertThat(result.nextCursor())
          .isNull();

      verify(chatParticipantPort, never())
          .updateLastReadMessageIdById(any(), any());
    }

    @Test
    @DisplayName("""
        4. 이미 최신 메시지까지 읽은 상태라면
          - 마지막으로 읽은 메시지가 갱신되지 않는다.
    """)
    void doesNotUpdateLastReadWhenAlreadyUpToDate() {
      // given
      when(
          chatParticipantPort.findLastReadMessageInfosByChatRoomId(CHAT_ROOM_ID)
      ).thenReturn(
          List.of(
              new ChatQueryDto.LastReadMessageInfo(USER_ID, USER_ID, CHAT_MESSAGE_ID)
          )
      );
      when(
          chatMessagePort.findAllByChatRoomIdAndCursor(eq(CHAT_ROOM_ID), eq(PAGE_SIZE), any())
      ).thenReturn(
          List.of(
              ChatMessage.of(
                  CHAT_MESSAGE_ID, CHAT_ROOM_ID, USER_ID, ChatMessage.MessageType.TEXT, "hi", null, Instant.now()
              )
          )
      );

      // when
      chatMessageService.getChatMessagesInfo(USER_ID, CHAT_ROOM_ID, PAGE_SIZE, null);

      // then
      verify(chatParticipantPort, never())
          .updateLastReadMessageIdById(any(), any());
    }

    @Test
    @DisplayName("""
        5. 커서가 null이면
          - id와 createdAt이 null인 커서로 조회한다
    """)
    void queriesWithEmptyCursorWhenDtoIsNull() {
      // given
      when(
          chatParticipantPort.findLastReadMessageInfosByChatRoomId(CHAT_ROOM_ID)
      ).thenReturn(
          List.of(
              new ChatQueryDto.LastReadMessageInfo(USER_ID, USER_ID, CHAT_MESSAGE_ID)
          )
      );
      when(
          chatMessagePort.findAllByChatRoomIdAndCursor(any(), anyInt(), any())
      ).thenReturn(
          List.of()
      );

      // when
      chatMessageService.getChatMessagesInfo(USER_ID, CHAT_ROOM_ID, PAGE_SIZE, null);

      // then
      ArgumentCaptor<ChatQueryDto.ChatMessageCursor> cursorCaptor = ArgumentCaptor.forClass(ChatQueryDto.ChatMessageCursor.class);
      verify(chatMessagePort)
          .findAllByChatRoomIdAndCursor(eq(CHAT_ROOM_ID), eq(PAGE_SIZE), cursorCaptor.capture());
      assertThat(cursorCaptor.getValue().id())
          .isNull();
      assertThat(cursorCaptor.getValue().createdAt())
          .isNull();
    }
  }
}
