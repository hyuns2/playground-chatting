package io.playground.chatservice.infrastructure.jpa.adapter;

import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.infrastructure.jpa.repository.ChatRoomRepository;
import io.playground.chatservice.testsupport.DataJpaTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = DataJpaTestConfig.class)
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "spring.sql.init.mode=never"
})
@Import(ChatRoomPersistenceAdapter.class)
@DisplayName("ChatRoomPersistenceAdapter 통합 테스트")
class ChatRoomPersistenceAdapterTest {

    @Autowired
    private ChatRoomPersistenceAdapter sut;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("채팅방을 저장하면 id가 채워진 도메인 객체를 반환한다")
    void savesChatRoom() {
        ChatRoom saved = sut.save(ChatRoom.of(null, ChatRoom.RoomType.GROUP, "room", null));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getType()).isEqualTo(ChatRoom.RoomType.GROUP);
        assertThat(saved.getName()).isEqualTo("room");
    }

    @Test
    @DisplayName("updateLastMessagedAtByChatRoomId는 벌크 업데이트로 최종 발신 시각을 갱신한다")
    void updatesLastMessagedAt() {
        ChatRoom saved = sut.save(ChatRoom.of(null, ChatRoom.RoomType.GROUP, "room", null));
        Instant lastMessagedAt = Instant.parse("2026-01-01T00:00:00Z");

        sut.updateLastMessagedAtByChatRoomId(saved.getId(), lastMessagedAt);
        entityManager.clear();

        assertThat(chatRoomRepository.findById(saved.getId()).orElseThrow().getLastMessagedAt())
                .isEqualTo(lastMessagedAt);
    }
}
