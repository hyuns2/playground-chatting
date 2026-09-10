package io.playground.chatservice.infrastructure.jpa.adapter;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.domain.ChatMessage;
import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.infrastructure.jpa.entity.ChatRoomEntity;
import io.playground.chatservice.infrastructure.jpa.repository.ChatRoomRepository;
import io.playground.chatservice.testsupport.DataJpaTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = DataJpaTestConfig.class)
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "spring.sql.init.mode=never"
})
@Import(ChatMessagePersistenceAdapter.class)
@DisplayName("ChatMessagePersistenceAdapter 통합 테스트")
class ChatMessagePersistenceAdapterTest {

    @Autowired
    private ChatMessagePersistenceAdapter sut;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    private Long chatRoomId;

    @BeforeEach
    void setUp() {
        ChatRoomEntity chatRoom = chatRoomRepository.save(
                new ChatRoomEntity(null, ChatRoom.RoomType.GROUP, "room", null)
        );
        chatRoomId = chatRoom.getId();
    }

    @Test
    @DisplayName("parentMessageId 없이 저장하면 id와 createdAt이 채워진 도메인 객체를 반환한다")
    void savesMessageWithoutParent() {
        ChatMessage saved = sut.save(
                ChatMessage.of(null, chatRoomId, 1L, ChatMessage.MessageType.TEXT, "hello", null, null)
        );

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(saved.getParentMessageId()).isNull();
    }

    @Test
    @DisplayName("parentMessageId와 함께 저장하면 부모 메시지 id가 유지된다")
    void savesMessageWithParent() {
        ChatMessage parent = sut.save(
                ChatMessage.of(null, chatRoomId, 1L, ChatMessage.MessageType.TEXT, "parent", null, null)
        );

        ChatMessage child = sut.save(
                ChatMessage.of(null, chatRoomId, 2L, ChatMessage.MessageType.TEXT, "child", parent.getId(), null)
        );

        assertThat(child.getParentMessageId()).isEqualTo(parent.getId());
    }

    @Test
    @DisplayName("existsByIdAndChatRoomId는 같은 채팅방의 메시지에서만 true를 반환한다")
    void checksExistenceScopedByChatRoom() {
        ChatMessage saved = sut.save(
                ChatMessage.of(null, chatRoomId, 1L, ChatMessage.MessageType.TEXT, "hello", null, null)
        );
        ChatRoomEntity otherRoom = chatRoomRepository.save(
                new ChatRoomEntity(null, ChatRoom.RoomType.GROUP, "other", null)
        );

        assertThat(sut.existsByIdAndChatRoomId(saved.getId(), chatRoomId)).isTrue();
        assertThat(sut.existsByIdAndChatRoomId(saved.getId(), otherRoom.getId())).isFalse();
        assertThat(sut.existsByIdAndChatRoomId(999L, chatRoomId)).isFalse();
    }

    @Test
    @DisplayName("커서 없이 조회하면 최신 메시지부터 size만큼 반환한다")
    void findsFirstPageWithoutCursor() {
        for (int i = 0; i < 3; i++)
            sut.save(ChatMessage.of(null, chatRoomId, 1L, ChatMessage.MessageType.TEXT, "msg" + i, null, null));

        List<ChatMessage> result = sut.findAllByChatRoomIdAndCursor(
                chatRoomId, 2, new ChatQueryDto.ChatMessageCursor(null, null)
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isGreaterThan(result.get(1).getId());
    }

    @Test
    @DisplayName("커서를 지정하면 그보다 이전 메시지만 반환한다")
    void findsNextPageWithCursor() {
        ChatMessage first = sut.save(ChatMessage.of(null, chatRoomId, 1L, ChatMessage.MessageType.TEXT, "msg0", null, null));
        ChatMessage second = sut.save(ChatMessage.of(null, chatRoomId, 1L, ChatMessage.MessageType.TEXT, "msg1", null, null));

        List<ChatMessage> result = sut.findAllByChatRoomIdAndCursor(
                chatRoomId, 10, new ChatQueryDto.ChatMessageCursor(second.getId(), second.getCreatedAt())
        );

        assertThat(result).extracting(ChatMessage::getId).containsExactly(first.getId());
    }
}
