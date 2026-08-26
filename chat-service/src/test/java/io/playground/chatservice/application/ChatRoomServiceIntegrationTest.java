package io.playground.chatservice.application;

import io.playground.chatservice.application.chat.command.CreateChatRoomCommand;
import io.playground.chatservice.application.chat.dto.ChatDto;
import io.playground.chatservice.application.chat.service.ChatRoomService;
import io.playground.chatservice.domain.chat.room.ChatRoom;
import io.playground.chatservice.infrastructure.config.BaseIntegrationTest;
import io.playground.chatservice.infrastructure.messaging.producer.EventLogJpaRepository;
import io.playground.chatservice.infrastructure.persistence.chat.entity.ChatParticipantJpaEntity;
import io.playground.chatservice.infrastructure.persistence.chat.repository.ChatParticipantJpaRepository;
import io.playground.chatservice.infrastructure.persistence.chat.repository.ChatRoomJpaRepository;
import io.playground.chatservice.infrastructure.persistence.view.entity.UserViewJpaEntity;
import io.playground.chatservice.infrastructure.persistence.view.repository.UserViewJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Transactional
public class ChatRoomServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    ChatRoomService service;
    @Autowired
    UserViewJpaRepository userViewRepository;
    @Autowired
    ChatRoomJpaRepository chatRoomRepository;
    @Autowired
    ChatParticipantJpaRepository chatParticipantRepository;
    @Autowired
    EventLogJpaRepository eventLogJpaRepository;
    @PersistenceContext
    EntityManager entityManager;

    @BeforeEach
    void beforeEach() {
        userViewRepository.saveAll(List.of(
                UserViewJpaEntity.of("f52b1459-7061-465b-b230-b5bbce14e553", "유저1", true, LocalDateTime.now()),
                UserViewJpaEntity.of("46700624-2054-4df6-a9f4-10424a46eccd", "유저2", true, LocalDateTime.now()),
                UserViewJpaEntity.of("68e000ef-e717-4b1e-97f1-1baabafda524", "유저3", true, LocalDateTime.now())
        ));
    }

    @AfterEach
    void afterEach() {
        entityManager.flush();
        entityManager.clear();

        chatRoomRepository.deleteAll();
        chatParticipantRepository.deleteAll();
        eventLogJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("채팅방 생성 후, 채팅방과 참여자 정보가 DB에 저장되는지 확인")
    void createChatRoom_엔티티_저장_확인() {
        // given
        CreateChatRoomCommand command = CreateChatRoomCommand.of(
                "f52b1459-7061-465b-b230-b5bbce14e553",
                ChatRoom.RoomType.GROUP,
                "테스트 채팅방1",
                new ArrayList<>(
                        List.of(
                                "46700624-2054-4df6-a9f4-10424a46eccd",
                                "68e000ef-e717-4b1e-97f1-1baabafda524"
                        )
                )
        );

        // when
        log.info("\n=== Test Start ===");
        Long chatRoomId = service.createChatRoom(command);

        entityManager.flush();
        entityManager.clear();
        log.info("\n=== Test End ===");

        // then
        List<ChatParticipantJpaEntity> entities =
                chatParticipantRepository.findAllByChatRoomId(chatRoomId);

        Assertions.assertThat(entities)
                .extracting(ChatParticipantJpaEntity::getParticipantId)
                .containsOnly(
                        "f52b1459-7061-465b-b230-b5bbce14e553",
                        "46700624-2054-4df6-a9f4-10424a46eccd",
                        "68e000ef-e717-4b1e-97f1-1baabafda524"
                );

        Assertions.assertThat(entities)
                .filteredOn(ChatParticipantJpaEntity::isAdmin)
                .extracting(ChatParticipantJpaEntity::getParticipantId)
                .containsOnly("f52b1459-7061-465b-b230-b5bbce14e553");
    }

    @Test
    @DisplayName("채팅방 2개 생성 후, 모두 참여한 참가자 기준 조회")
    void getChatRooms() {
        // given
        service.createChatRoom(CreateChatRoomCommand.of(
                "f52b1459-7061-465b-b230-b5bbce14e553",
                ChatRoom.RoomType.GROUP,
                "그룹 채팅방",
                new ArrayList<>(
                        List.of(
                                "46700624-2054-4df6-a9f4-10424a46eccd",
                                "68e000ef-e717-4b1e-97f1-1baabafda524"
                        )
                )
        ));
        service.createChatRoom(CreateChatRoomCommand.of(
                "46700624-2054-4df6-a9f4-10424a46eccd",
                ChatRoom.RoomType.PRIVATE,
                "1:1 채팅방",
                new ArrayList<>(List.of(
                        "68e000ef-e717-4b1e-97f1-1baabafda524"))
        ));

        entityManager.flush();
        entityManager.clear();

        // when
        StopWatch watch = new StopWatch();
        log.info("\n=== Test Start ===");
        watch.start("쿼리 최적화 후 채팅방 조회");

        List<ChatDto.ChatRoomInfo> result =
                service.getChatRooms("46700624-2054-4df6-a9f4-10424a46eccd");

        watch.stop();
        log.info("\n=== Test End ===");
        System.out.println(watch.prettyPrint());

        // then
        for (ChatDto.ChatRoomInfo dto: result) {
            if (dto.getType() == ChatRoom.RoomType.GROUP) {
                Assertions.assertThat(dto.getName()).isEqualTo("그룹 채팅방");
                Assertions.assertThat(dto.getParticipantInfos())
                        .extracting(ChatDto.ParticipantInfo::getId)
                        .containsOnly(
                                "f52b1459-7061-465b-b230-b5bbce14e553",
                                "46700624-2054-4df6-a9f4-10424a46eccd",
                                "68e000ef-e717-4b1e-97f1-1baabafda524"
                        );
                Assertions.assertThat(dto.getParticipantInfos())
                        .filteredOn(ChatDto.ParticipantInfo::isAdmin)
                        .extracting(ChatDto.ParticipantInfo::getId)
                        .containsOnly("f52b1459-7061-465b-b230-b5bbce14e553");
            }

            else {
                Assertions.assertThat(dto.getName()).isEqualTo("1:1 채팅방");
                Assertions.assertThat(dto.getParticipantInfos())
                        .extracting(ChatDto.ParticipantInfo::getId)
                        .containsOnly(
                                "46700624-2054-4df6-a9f4-10424a46eccd",
                                "68e000ef-e717-4b1e-97f1-1baabafda524"
                        );
                Assertions.assertThat(dto.getParticipantInfos())
                        .filteredOn(ChatDto.ParticipantInfo::isAdmin)
                        .extracting(ChatDto.ParticipantInfo::getId)
                        .containsOnly("46700624-2054-4df6-a9f4-10424a46eccd");
            }
        }
    }
}
