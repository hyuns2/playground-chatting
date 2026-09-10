package io.playground.chatservice.infrastructure.jpa.adapter;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.domain.ChatParticipant;
import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.infrastructure.jpa.entity.ChatRoomEntity;
import io.playground.chatservice.infrastructure.jpa.repository.ChatParticipantRepository;
import io.playground.chatservice.infrastructure.jpa.repository.ChatRoomRepository;
import io.playground.chatservice.testsupport.DataJpaTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
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
@Import(ChatParticipantPersistenceAdapter.class)
@DisplayName("ChatParticipantPersistenceAdapter 통합 테스트")
class ChatParticipantPersistenceAdapterTest {

    @Autowired
    private ChatParticipantPersistenceAdapter sut;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("존재하는 참여자/채팅방 조합만 true를 반환한다")
    void checksParticipantExistence() {
        ChatRoomEntity chatRoom = chatRoomRepository.save(
                new ChatRoomEntity(null, ChatRoom.RoomType.GROUP, "room", null)
        );
        sut.saveAll(List.of(
                ChatParticipant.of(null, chatRoom.getId(), USER_ID, "me", true, null)
        ));

        assertThat(sut.existsByChatRoomIdAndParticipantId(chatRoom.getId(), USER_ID)).isTrue();
        assertThat(sut.existsByChatRoomIdAndParticipantId(chatRoom.getId(), 999L)).isFalse();
    }

    @Test
    @DisplayName("saveAll로 저장한 참여자는 findAllByChatRoomIds로 조회된다")
    void savesAndFindsParticipants() {
        ChatRoomEntity chatRoom = chatRoomRepository.save(
                new ChatRoomEntity(null, ChatRoom.RoomType.GROUP, "room", null)
        );

        sut.saveAll(List.of(
                ChatParticipant.of(null, chatRoom.getId(), USER_ID, "me", true, null),
                ChatParticipant.of(null, chatRoom.getId(), 2L, "other", false, null)
        ));

        List<ChatParticipant> result = sut.findAllByChatRoomIds(List.of(chatRoom.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ChatParticipant::getParticipantId)
                .containsExactlyInAnyOrder(USER_ID, 2L);
    }

    @Test
    @DisplayName("findChatRoomsByParticipantId는 최종 발신 시각 내림차순으로 정렬해서 반환한다")
    void findsChatRoomsSortedByLastMessagedAtDesc() {
        ChatRoomEntity older = chatRoomRepository.save(
                new ChatRoomEntity(null, ChatRoom.RoomType.GROUP, "older", Instant.parse("2026-01-01T00:00:00Z"))
        );
        ChatRoomEntity newer = chatRoomRepository.save(
                new ChatRoomEntity(null, ChatRoom.RoomType.GROUP, "newer", Instant.parse("2026-02-01T00:00:00Z"))
        );
        sut.saveAll(List.of(
                ChatParticipant.of(null, older.getId(), USER_ID, "me", true, null),
                ChatParticipant.of(null, newer.getId(), USER_ID, "me", true, null)
        ));

        List<ChatRoom> result = sut.findChatRoomsByParticipantId(USER_ID, 0, 10);

        assertThat(result).extracting(ChatRoom::getId)
                .containsExactly(newer.getId(), older.getId());
    }

    @Test
    @DisplayName("findLastReadMessageInfosByChatRoomId는 채팅방의 모든 참여자 정보를 반환한다")
    void findsLastReadMessageInfos() {
        ChatRoomEntity chatRoom = chatRoomRepository.save(
                new ChatRoomEntity(null, ChatRoom.RoomType.GROUP, "room", null)
        );
        sut.saveAll(List.of(
                ChatParticipant.of(null, chatRoom.getId(), USER_ID, "me", true, 5L)
        ));

        List<ChatQueryDto.LastReadMessageInfo> result =
                sut.findLastReadMessageInfosByChatRoomId(chatRoom.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().participantId()).isEqualTo(USER_ID);
        assertThat(result.getFirst().lastReadMessageId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("updateLastReadMessageIdById는 벌크 업데이트로 마지막으로 읽은 메시지를 갱신한다")
    void updatesLastReadMessageId() {
        ChatRoomEntity chatRoom = chatRoomRepository.save(
                new ChatRoomEntity(null, ChatRoom.RoomType.GROUP, "room", null)
        );
        sut.saveAll(List.of(
                ChatParticipant.of(null, chatRoom.getId(), USER_ID, "me", true, null)
        ));
        Long participantId = chatParticipantRepository
                .findAllByChatRoom_IdIn(List.of(chatRoom.getId()))
                .getFirst().getId();

        sut.updateLastReadMessageIdById(participantId, 42L);
        entityManager.clear();

        assertThat(chatParticipantRepository.findById(participantId).orElseThrow().getLastReadMessageId())
                .isEqualTo(42L);
    }
}
