package io.playground.chatservice.application.usecase;

import io.playground.chatservice.application.event.ChatEventDto;
import io.playground.chatservice.application.event.EventProducerPort;
import io.playground.chatservice.domain.ChatMessage;
import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.exception.BusinessErrorCode;
import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.infrastructure.jpa.adapter.ChatMessagePersistenceAdapter;
import io.playground.chatservice.infrastructure.jpa.adapter.ChatParticipantPersistenceAdapter;
import io.playground.chatservice.infrastructure.jpa.adapter.ChatRoomPersistenceAdapter;
import io.playground.chatservice.infrastructure.jpa.entity.ChatMessageEntity;
import io.playground.chatservice.infrastructure.jpa.entity.ChatParticipantEntity;
import io.playground.chatservice.infrastructure.jpa.entity.ChatRoomEntity;
import io.playground.chatservice.infrastructure.jpa.repository.ChatMessageRepository;
import io.playground.chatservice.infrastructure.jpa.repository.ChatParticipantRepository;
import io.playground.chatservice.infrastructure.jpa.repository.ChatRoomRepository;
import io.playground.chatservice.presentation.ChatRequestDto;
import io.playground.chatservice.presentation.ChatResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * ChatMessageService를 실제 JPA 어댑터, H2 인메모리 DB와 함께 구동하는 통합 테스트.
 * EventProducerPort는 outbox(JPA) 구현체 대신 목으로 대체해 슬라이스 범위를 벗어나지 않게 한다.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "spring.sql.init.mode=never"
})
@Import({
        ChatMessageService.class,
        ChatMessagePersistenceAdapter.class,
        ChatParticipantPersistenceAdapter.class,
        ChatRoomPersistenceAdapter.class
})
@DisplayName("ChatMessageService 통합 테스트")
class ChatMessageServiceIntegrationTest {

    @Autowired
    private ChatMessageService sut;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @MockitoBean
    private EventProducerPort eventProducerPort;

    private static final Long SENDER_ID = 1L;
    private static final Long OTHER_PARTICIPANT_ID = 2L;

    private ChatRoomEntity chatRoom;
    private ChatParticipantEntity senderParticipant;

    @BeforeEach
    void setUp() {
        chatRoom = chatRoomRepository.save(
                new ChatRoomEntity(null, ChatRoom.RoomType.GROUP, "room", null)
        );
        senderParticipant = chatParticipantRepository.save(
                new ChatParticipantEntity(null, chatRoom, SENDER_ID, "me", true, null)
        );
        chatParticipantRepository.save(
                new ChatParticipantEntity(null, chatRoom, OTHER_PARTICIPANT_ID, "other", false, null)
        );
    }

    @Test
    @DisplayName("정상 참여자가 메시지를 보내면 DB에 저장되고 채팅방 최종 발신 시각이 갱신되며 이벤트가 발행된다")
    void sendsMessageAndPersistsIt() {
        ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
                ChatMessage.MessageType.TEXT, "hello", null
        );

        Long messageId = sut.sendChatMessage(SENDER_ID, chatRoom.getId(), dto);

        // updateLastMessagedAtByChatRoomId is a bulk @Modifying update, which bypasses the
        // persistence context cache; clear it so the re-fetch below sees the committed value.
        entityManager.clear();

        ChatMessageEntity saved = chatMessageRepository.findById(messageId).orElseThrow();
        assertThat(saved.getContent()).isEqualTo("hello");
        assertThat(saved.getSenderId()).isEqualTo(SENDER_ID);
        assertThat(saved.getChatRoom().getId()).isEqualTo(chatRoom.getId());

        ChatRoomEntity updatedRoom = chatRoomRepository.findById(chatRoom.getId()).orElseThrow();
        assertThat(updatedRoom.getLastMessagedAt()).isNotNull();

        verify(eventProducerPort).produce(
                any(), eq(ChatEventDto.EventType.CHAT_MESSAGE_SENT), any(), any()
        );
    }

    @Test
    @DisplayName("parentMessageId가 같은 채팅방의 메시지를 가리키면 부모 메시지와 함께 저장된다")
    void sendsMessageWithParent() {
        Long parentId = sut.sendChatMessage(
                SENDER_ID, chatRoom.getId(),
                new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "parent", null)
        );

        Long childId = sut.sendChatMessage(
                OTHER_PARTICIPANT_ID, chatRoom.getId(),
                new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "child", parentId)
        );

        ChatMessageEntity child = chatMessageRepository.findById(childId).orElseThrow();
        assertThat(child.getParentMessage().getId()).isEqualTo(parentId);
    }

    @Test
    @DisplayName("채팅방 참여자가 아닌 유저가 메시지를 보내면 예외가 발생하고 아무것도 저장되지 않는다")
    void throwsWhenSenderIsNotParticipant() {
        Long nonParticipantId = 999L;
        ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
                ChatMessage.MessageType.TEXT, "hello", null
        );

        assertThatThrownBy(() -> sut.sendChatMessage(nonParticipantId, chatRoom.getId(), dto))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);

        assertThat(chatMessageRepository.count()).isZero();
        verifyNoInteractions(eventProducerPort);
    }

    @Test
    @DisplayName("존재하지 않는 parentMessageId면 예외가 발생한다")
    void throwsWhenParentMessageDoesNotExist() {
        ChatRequestDto.SendChatMessage dto = new ChatRequestDto.SendChatMessage(
                ChatMessage.MessageType.TEXT, "hello", 12345L
        );

        assertThatThrownBy(() -> sut.sendChatMessage(SENDER_ID, chatRoom.getId(), dto))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(BusinessErrorCode.INVALID_PARENT_MESSAGE);

        assertThat(chatMessageRepository.count()).isZero();
    }

    @Test
    @DisplayName("메시지 목록을 조회하면 최근 순으로 반환되고 조회자의 마지막으로 읽은 메시지가 갱신된다")
    void getsChatMessagesInfoAndUpdatesLastRead() {
        Long firstId = sut.sendChatMessage(
                OTHER_PARTICIPANT_ID, chatRoom.getId(),
                new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "msg1", null)
        );
        Long secondId = sut.sendChatMessage(
                OTHER_PARTICIPANT_ID, chatRoom.getId(),
                new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "msg2", null)
        );

        ChatResponseDto.GetChatMessagesInfo result =
                sut.getChatMessagesInfo(SENDER_ID, chatRoom.getId(), 10, null);

        assertThat(result.chatMessageInfos())
                .extracting(ChatResponseDto.ChatMessageInfo::id)
                .containsExactly(secondId, firstId);
        assertThat(result.lastReadMessageInfos()).hasSize(2);

        // updateLastReadMessageIdById is a bulk @Modifying update, which bypasses the
        // persistence context cache; clear it so the re-fetch below sees the committed value.
        entityManager.clear();

        ChatParticipantEntity refreshedSender =
                chatParticipantRepository.findById(senderParticipant.getId()).orElseThrow();
        assertThat(refreshedSender.getLastReadMessageId()).isEqualTo(secondId);
    }

    @Test
    @DisplayName("채팅방에 메시지가 없으면 다음 커서는 null이고 마지막으로 읽은 메시지는 갱신되지 않는다")
    void returnsNullCursorWhenNoMessages() {
        ChatResponseDto.GetChatMessagesInfo result =
                sut.getChatMessagesInfo(SENDER_ID, chatRoom.getId(), 10, null);

        assertThat(result.chatMessageInfos()).isEmpty();
        assertThat(result.nextCursor()).isNull();

        ChatParticipantEntity refreshedSender =
                chatParticipantRepository.findById(senderParticipant.getId()).orElseThrow();
        assertThat(refreshedSender.getLastReadMessageId()).isNull();
    }

    @Test
    @DisplayName("채팅방 참여자가 아닌 유저가 메시지 목록을 조회하면 예외가 발생한다")
    void throwsWhenViewerIsNotParticipant() {
        assertThatThrownBy(() -> sut.getChatMessagesInfo(999L, chatRoom.getId(), 10, null))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);
    }

    @Test
    @DisplayName("size보다 메시지가 많으면 size만큼만 최신 순으로 반환된다")
    void limitsMessagesBySize() {
        for (int i = 0; i < 5; i++) {
            sut.sendChatMessage(
                    OTHER_PARTICIPANT_ID, chatRoom.getId(),
                    new ChatRequestDto.SendChatMessage(ChatMessage.MessageType.TEXT, "msg" + i, null)
            );
        }

        ChatResponseDto.GetChatMessagesInfo result =
                sut.getChatMessagesInfo(SENDER_ID, chatRoom.getId(), 3, null);

        assertThat(result.chatMessageInfos()).hasSize(3);
    }
}
