package io.playground.chatservice.application.usecase;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.application.event.ChatEventDto;
import io.playground.chatservice.application.event.EventProducerPort;
import io.playground.chatservice.application.port.ChatMessagePersistencePort;
import io.playground.chatservice.application.port.ChatParticipantPersistencePort;
import io.playground.chatservice.application.port.ChatRoomPersistencePort;
import io.playground.chatservice.domain.ChatMessage;
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

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

    private ChatMessageService sut;

    private static final Long SENDER_ID = 1L;
    private static final Long CHAT_ROOM_ID = 10L;

    @BeforeEach
    void setUp() {
        sut = new ChatMessageService(
                chatRoomPort,
                chatParticipantPort,
                chatMessagePort,
                eventProducerPort
        );
    }

    @Nested
    @DisplayName("sendChatMessage")
    class SendChatMessage {

        @Test
        @DisplayName("정상 참여자가 메시지를 보내면 메시지가 저장되고, 채팅방 최종 발신 시각이 갱신되며, 이벤트가 발행된다")
        void sendsMessageAndUpdatesRoomAndProducesEvent() {
            ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
                    ChatMessage.MessageType.TEXT, "hello", null
            );
            Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
            ChatMessage savedMessage = ChatMessage.of(
                    100L, CHAT_ROOM_ID, SENDER_ID, dto.type(), dto.content(), null, createdAt
            );

            when(chatParticipantPort.existsByChatRoomIdAndParticipantId(CHAT_ROOM_ID, SENDER_ID))
                    .thenReturn(true);
            when(chatMessagePort.save(any(ChatMessage.class))).thenReturn(savedMessage);

            Long result = sut.sendChatMessage(SENDER_ID, CHAT_ROOM_ID, dto);

            assertThat(result).isEqualTo(100L);

            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
            verify(chatMessagePort).save(messageCaptor.capture());
            ChatMessage toSave = messageCaptor.getValue();
            assertThat(toSave.getId()).isNull();
            assertThat(toSave.getChatRoomId()).isEqualTo(CHAT_ROOM_ID);
            assertThat(toSave.getSenderId()).isEqualTo(SENDER_ID);
            assertThat(toSave.getType()).isEqualTo(dto.type());
            assertThat(toSave.getContent()).isEqualTo(dto.content());
            assertThat(toSave.getParentMessageId()).isNull();

            verify(chatRoomPort).updateLastMessagedAtByChatRoomId(CHAT_ROOM_ID, createdAt);

            ArgumentCaptor<ChatEventDto.ChatMessageSent> payloadCaptor =
                    ArgumentCaptor.forClass(ChatEventDto.ChatMessageSent.class);
            verify(eventProducerPort).produce(
                    anyString(),
                    eq(ChatEventDto.EventType.CHAT_MESSAGE_SENT),
                    any(Instant.class),
                    payloadCaptor.capture()
            );
            assertThat(payloadCaptor.getValue().chatMessageId()).isEqualTo(100L);
            assertThat(payloadCaptor.getValue().chatRoomId()).isEqualTo(CHAT_ROOM_ID);

            verify(chatMessagePort, never()).existsByIdAndChatRoomId(any(), any());
        }

        @Test
        @DisplayName("parentMessageId가 같은 채팅방에 존재하면 정상적으로 메시지가 저장된다")
        void sendsMessageWithValidParentMessage() {
            Long parentMessageId = 5L;
            ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
                    ChatMessage.MessageType.TEXT, "reply", parentMessageId
            );
            ChatMessage savedMessage = ChatMessage.of(
                    101L, CHAT_ROOM_ID, SENDER_ID, dto.type(), dto.content(), parentMessageId, Instant.now()
            );

            when(chatParticipantPort.existsByChatRoomIdAndParticipantId(CHAT_ROOM_ID, SENDER_ID))
                    .thenReturn(true);
            when(chatMessagePort.existsByIdAndChatRoomId(parentMessageId, CHAT_ROOM_ID))
                    .thenReturn(true);
            when(chatMessagePort.save(any(ChatMessage.class))).thenReturn(savedMessage);

            Long result = sut.sendChatMessage(SENDER_ID, CHAT_ROOM_ID, dto);

            assertThat(result).isEqualTo(101L);
            verify(chatMessagePort).existsByIdAndChatRoomId(parentMessageId, CHAT_ROOM_ID);
        }

        @Test
        @DisplayName("발신자가 채팅방 참여자가 아니면 예외가 발생하고 이후 로직은 수행되지 않는다")
        void throwsWhenSenderIsNotParticipant() {
            ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
                    ChatMessage.MessageType.TEXT, "hello", null
            );
            when(chatParticipantPort.existsByChatRoomIdAndParticipantId(CHAT_ROOM_ID, SENDER_ID))
                    .thenReturn(false);

            assertThatThrownBy(() -> sut.sendChatMessage(SENDER_ID, CHAT_ROOM_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting(exception -> ((BusinessException) exception).getErrorCode())
                    .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);

            verify(chatMessagePort, never()).save(any());
            verify(chatRoomPort, never()).updateLastMessagedAtByChatRoomId(any(), any());
            verifyNoInteractions(eventProducerPort);
        }

        @Test
        @DisplayName("parentMessageId가 같은 채팅방에 존재하지 않으면 예외가 발생한다")
        void throwsWhenParentMessageIsInvalid() {
            Long parentMessageId = 999L;
            ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
                    ChatMessage.MessageType.TEXT, "reply", parentMessageId
            );
            when(chatParticipantPort.existsByChatRoomIdAndParticipantId(CHAT_ROOM_ID, SENDER_ID))
                    .thenReturn(true);
            when(chatMessagePort.existsByIdAndChatRoomId(parentMessageId, CHAT_ROOM_ID))
                    .thenReturn(false);

            assertThatThrownBy(() -> sut.sendChatMessage(SENDER_ID, CHAT_ROOM_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting(exception -> ((BusinessException) exception).getErrorCode())
                    .isEqualTo(BusinessErrorCode.INVALID_PARENT_MESSAGE);

            verify(chatMessagePort, never()).save(any());
            verifyNoInteractions(eventProducerPort);
        }
    }

    @Nested
    @DisplayName("getChatMessagesInfo")
    class GetChatMessagesInfo {

        @Test
        @DisplayName("아직 안 읽은 메시지가 있으면 최신 메시지 id로 마지막으로 읽은 메시지가 갱신된다")
        void updatesLastReadMessageWhenNewMessageExists() {
            ChatQueryDto.LastReadMessageInfo currentUser =
                    new ChatQueryDto.LastReadMessageInfo(50L, SENDER_ID, null);
            ChatQueryDto.LastReadMessageInfo other =
                    new ChatQueryDto.LastReadMessageInfo(51L, 2L, 200L);
            when(chatParticipantPort.findLastReadMessageInfosByChatRoomId(CHAT_ROOM_ID))
                    .thenReturn(List.of(currentUser, other));

            ChatMessage latest = ChatMessage.of(
                    300L, CHAT_ROOM_ID, 2L, ChatMessage.MessageType.TEXT, "hi", null, Instant.now()
            );
            ChatMessage older = ChatMessage.of(
                    299L, CHAT_ROOM_ID, 2L, ChatMessage.MessageType.TEXT, "hello", null, Instant.now().minusSeconds(10)
            );
            when(chatMessagePort.findAllByChatRoomIdAndCursor(eq(CHAT_ROOM_ID), eq(20), any()))
                    .thenReturn(List.of(latest, older));

            ChatResponseDto.GetChatMessagesInfo result =
                    sut.getChatMessagesInfo(SENDER_ID, CHAT_ROOM_ID, 20, null);

            assertThat(result.chatMessageInfos()).hasSize(2);
            assertThat(result.chatMessageInfos().get(0).id()).isEqualTo(300L);
            assertThat(result.nextCursor().id()).isEqualTo(299L);
            assertThat(result.lastReadMessageInfos()).hasSize(2);

            verify(chatParticipantPort).updateLastReadMessageIdById(50L, 300L);
        }

        @Test
        @DisplayName("요청자가 채팅방 참여자가 아니면 예외가 발생한다")
        void throwsWhenRequesterIsNotParticipant() {
            when(chatParticipantPort.findLastReadMessageInfosByChatRoomId(CHAT_ROOM_ID))
                    .thenReturn(List.of(
                            new ChatQueryDto.LastReadMessageInfo(51L, 2L, null)
                    ));

            assertThatThrownBy(() -> sut.getChatMessagesInfo(SENDER_ID, CHAT_ROOM_ID, 20, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting(exception -> ((BusinessException) exception).getErrorCode())
                    .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);

            verify(chatMessagePort, never()).findAllByChatRoomIdAndCursor(any(), anyInt(), any());
        }

        @Test
        @DisplayName("조회된 메시지가 없으면 다음 커서는 null이고 마지막으로 읽은 메시지는 갱신되지 않는다")
        void doesNotUpdateLastReadWhenNoMessages() {
            ChatQueryDto.LastReadMessageInfo currentUser =
                    new ChatQueryDto.LastReadMessageInfo(50L, SENDER_ID, null);
            when(chatParticipantPort.findLastReadMessageInfosByChatRoomId(CHAT_ROOM_ID))
                    .thenReturn(List.of(currentUser));
            when(chatMessagePort.findAllByChatRoomIdAndCursor(eq(CHAT_ROOM_ID), eq(20), any()))
                    .thenReturn(List.of());

            ChatResponseDto.GetChatMessagesInfo result =
                    sut.getChatMessagesInfo(SENDER_ID, CHAT_ROOM_ID, 20, null);

            assertThat(result.chatMessageInfos()).isEmpty();
            assertThat(result.nextCursor()).isNull();
            verify(chatParticipantPort, never()).updateLastReadMessageIdById(any(), any());
        }

        @Test
        @DisplayName("이미 최신 메시지까지 읽은 상태라면 마지막으로 읽은 메시지가 갱신되지 않는다")
        void doesNotUpdateLastReadWhenAlreadyUpToDate() {
            ChatQueryDto.LastReadMessageInfo currentUser =
                    new ChatQueryDto.LastReadMessageInfo(50L, SENDER_ID, 300L);
            when(chatParticipantPort.findLastReadMessageInfosByChatRoomId(CHAT_ROOM_ID))
                    .thenReturn(List.of(currentUser));

            ChatMessage message = ChatMessage.of(
                    300L, CHAT_ROOM_ID, 2L, ChatMessage.MessageType.TEXT, "hi", null, Instant.now()
            );
            when(chatMessagePort.findAllByChatRoomIdAndCursor(eq(CHAT_ROOM_ID), eq(20), any()))
                    .thenReturn(List.of(message));

            sut.getChatMessagesInfo(SENDER_ID, CHAT_ROOM_ID, 20, null);

            verify(chatParticipantPort, never()).updateLastReadMessageIdById(any(), any());
        }

        @Test
        @DisplayName("커서가 null이면 id와 createdAt이 null인 커서로 조회한다")
        void queriesWithEmptyCursorWhenDtoIsNull() {
            when(chatParticipantPort.findLastReadMessageInfosByChatRoomId(CHAT_ROOM_ID))
                    .thenReturn(List.of(
                            new ChatQueryDto.LastReadMessageInfo(50L, SENDER_ID, 1L)
                    ));
            when(chatMessagePort.findAllByChatRoomIdAndCursor(any(), anyInt(), any()))
                    .thenReturn(List.of());

            sut.getChatMessagesInfo(SENDER_ID, CHAT_ROOM_ID, 20, null);

            ArgumentCaptor<ChatQueryDto.ChatMessageCursor> cursorCaptor =
                    ArgumentCaptor.forClass(ChatQueryDto.ChatMessageCursor.class);
            verify(chatMessagePort).findAllByChatRoomIdAndCursor(eq(CHAT_ROOM_ID), eq(20), cursorCaptor.capture());
            assertThat(cursorCaptor.getValue().id()).isNull();
            assertThat(cursorCaptor.getValue().createdAt()).isNull();
        }
    }
}
