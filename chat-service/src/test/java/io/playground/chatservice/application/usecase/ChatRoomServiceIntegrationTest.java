package io.playground.chatservice.application.usecase;

import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.exception.BusinessErrorCode;
import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.infrastructure.jpa.adapter.ChatParticipantPersistenceAdapter;
import io.playground.chatservice.infrastructure.jpa.adapter.ChatRoomPersistenceAdapter;
import io.playground.chatservice.infrastructure.jpa.adapter.UserViewQueryAdapter;
import io.playground.chatservice.infrastructure.jpa.entity.UserViewEntity;
import io.playground.chatservice.infrastructure.jpa.repository.ChatParticipantRepository;
import io.playground.chatservice.infrastructure.jpa.repository.ChatRoomRepository;
import io.playground.chatservice.infrastructure.jpa.repository.UserViewRepository;
import io.playground.chatservice.presentation.ChatRequestDto;
import io.playground.chatservice.presentation.ChatResponseDto;
import io.playground.chatservice.testsupport.DataJpaTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ChatRoomService를 실제 JPA 어댑터, H2 인메모리 DB와 함께 구동하는 통합 테스트.
 */
@DataJpaTest
@ContextConfiguration(classes = DataJpaTestConfig.class)
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "spring.sql.init.mode=never"
})
@Import({
        ChatRoomService.class,
        ChatRoomPersistenceAdapter.class,
        ChatParticipantPersistenceAdapter.class,
        UserViewQueryAdapter.class,
        ChatRoomServiceIntegrationTest.ObjectMapperTestConfig.class
})
@DisplayName("ChatRoomService 통합 테스트")
class ChatRoomServiceIntegrationTest {

    @TestConfiguration
    static class ObjectMapperTestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private ChatRoomService sut;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private UserViewRepository userViewRepository;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        userViewRepository.save(new UserViewEntity(null, USER_ID, "me", Instant.now()));
        userViewRepository.save(new UserViewEntity(null, 2L, "user2", Instant.now()));
        userViewRepository.save(new UserViewEntity(null, 3L, "user3", Instant.now()));
    }

    @Test
    @DisplayName("모든 참여자가 존재하면 채팅방과 참여자가 저장되고 요청자는 관리자가 된다")
    void createsChatRoomWithParticipants() {
        ChatRequestDto.CreateChatRoom dto = new ChatRequestDto.CreateChatRoom(
                ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(2L, 3L)), "room"
        );

        Long chatRoomId = sut.createChatRoom(USER_ID, dto);

        assertThat(chatRoomRepository.findById(chatRoomId)).isPresent();
        assertThat(chatParticipantRepository.findAllByChatRoom_IdIn(List.of(chatRoomId)))
                .hasSize(3)
                .anySatisfy(participant -> {
                    assertThat(participant.getParticipantId()).isEqualTo(USER_ID);
                    assertThat(participant.isAdmin()).isTrue();
                });
    }

    @Test
    @DisplayName("존재하지 않는 참여자가 포함되면 채팅방을 생성하지 않고 예외가 발생한다")
    void throwsWhenParticipantNotFound() {
        ChatRequestDto.CreateChatRoom dto = new ChatRequestDto.CreateChatRoom(
                ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(2L, 999L)), "room"
        );

        assertThatThrownBy(() -> sut.createChatRoom(USER_ID, dto))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(BusinessErrorCode.PARTICIPANT_NOT_FOUND);

        assertThat(chatRoomRepository.count()).isZero();
    }

    @Test
    @DisplayName("사용자가 참여한 채팅방 목록을 참여자 정보와 함께 조회한다")
    void getsChatRoomInfosWithParticipants() {
        Long roomId = sut.createChatRoom(
                USER_ID,
                new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(2L)), "room")
        );

        List<ChatResponseDto.GetChatRoomInfo> result = sut.getChatRoomInfos(USER_ID, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(roomId);
        assertThat(result.getFirst().participantInfos())
                .extracting(ChatResponseDto.ParticipantInfo::id)
                .containsExactlyInAnyOrder(USER_ID, 2L);
    }

    @Test
    @DisplayName("참여하지 않은 사용자의 채팅방 목록은 비어있다")
    void returnsEmptyListForNonParticipant() {
        sut.createChatRoom(
                USER_ID,
                new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(2L)), "room")
        );

        List<ChatResponseDto.GetChatRoomInfo> result = sut.getChatRoomInfos(3L, 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("채팅방 참여자이면 예외가 발생하지 않는다")
    void doesNotThrowWhenParticipant() {
        Long roomId = sut.createChatRoom(
                USER_ID,
                new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(2L)), "room")
        );

        sut.validateChatParticipant(roomId, USER_ID);
    }

    @Test
    @DisplayName("채팅방 참여자가 아니면 예외가 발생한다")
    void throwsWhenNotParticipant() {
        Long roomId = sut.createChatRoom(
                USER_ID,
                new ChatRequestDto.CreateChatRoom(ChatRoom.RoomType.GROUP, new ArrayList<>(List.of(2L)), "room")
        );

        assertThatThrownBy(() -> sut.validateChatParticipant(roomId, 3L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(BusinessErrorCode.INVALID_CHAT_PARTICIPANT);
    }
}
