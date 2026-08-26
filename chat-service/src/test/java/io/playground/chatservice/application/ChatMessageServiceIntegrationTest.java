package io.playground.chatservice.application;

import io.playground.chatservice.application.chat.command.CreateChatRoomCommand;
import io.playground.chatservice.application.chat.command.GetChatMessagesCommand;
import io.playground.chatservice.application.chat.command.SendChatMessageCommand;
import io.playground.chatservice.application.chat.dto.ChatDto;
import io.playground.chatservice.application.chat.service.ChatMessageService;
import io.playground.chatservice.application.chat.service.ChatRoomService;
import io.playground.chatservice.application.eventstream.PubEventType;
import io.playground.chatservice.domain.chat.message.ChatMessage;
import io.playground.chatservice.domain.chat.room.ChatRoom;
import io.playground.chatservice.infrastructure.config.BaseIntegrationTest;
import io.playground.chatservice.infrastructure.messaging.producer.EventLogJpaEntity;
import io.playground.chatservice.infrastructure.messaging.producer.EventLogJpaRepository;
import io.playground.chatservice.infrastructure.persistence.chat.entity.ChatMessageJpaEntity;
import io.playground.chatservice.infrastructure.persistence.chat.entity.ChatParticipantJpaEntity;
import io.playground.chatservice.infrastructure.persistence.chat.repository.ChatMessageJpaRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Transactional
public class ChatMessageServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    ChatMessageService service;
    @Autowired
    ChatRoomService chatRoomService;
    @Autowired
    UserViewJpaRepository userViewRepository;
    @Autowired
    ChatRoomJpaRepository chatRoomRepository;
    @Autowired
    ChatParticipantJpaRepository chatParticipantRepository;
    @Autowired
    ChatMessageJpaRepository chatMessageRepository;
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

        chatMessageRepository.deleteAll();
        chatParticipantRepository.deleteAll();
        chatRoomRepository.deleteAll();
        eventLogJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("채팅메시지 생성 후, 즉시 DB에 저장되는지 확인")
    void sendChatMessage_엔티티_저장_확인() {
        // given
        Long chatRoomId = chatRoomService.createChatRoom(
                CreateChatRoomCommand.of(
                        "f52b1459-7061-465b-b230-b5bbce14e553",
                        ChatRoom.RoomType.GROUP,
                        "테스트 채팅방1",
                        new ArrayList<>(
                                List.of(
                                        "46700624-2054-4df6-a9f4-10424a46eccd",
                                        "68e000ef-e717-4b1e-97f1-1baabafda524"
                                )
                        )
                )
        );

        entityManager.flush();
        entityManager.clear();

        SendChatMessageCommand command = SendChatMessageCommand.of(
                "f52b1459-7061-465b-b230-b5bbce14e553",
                chatRoomId,
                ChatMessage.MessageType.TEXT,
                "안녕하세요1",
                null
        );

        // when
        log.info("\n=== Test Start ===");

        Long chatMessageId = service.sendChatMessage(command);
        entityManager.flush();
        entityManager.clear();

        service.sendChatMessage(SendChatMessageCommand.of(
                "46700624-2054-4df6-a9f4-10424a46eccd",
                chatRoomId,
                ChatMessage.MessageType.TEXT,
                "안녕하세요2",
                chatMessageId
        ));
        entityManager.flush();
        entityManager.clear();

        log.info("\n=== Test End ===");

        // then
        List<ChatMessageJpaEntity> result =
                chatMessageRepository.findPageByChatRoomIdOrderByCreatedAtDesc(
                        chatRoomId,
                        PageRequest.of(0, 10)
                );

        Assertions.assertThat(result)
                .extracting(ChatMessageJpaEntity::getContent)
                .containsExactly(
                        "안녕하세요2",
                        "안녕하세요1"
                );

        Assertions.assertThat(result)
                .extracting(ChatMessageJpaEntity::getSenderId)
                .containsExactly(
                        "46700624-2054-4df6-a9f4-10424a46eccd",
                        "f52b1459-7061-465b-b230-b5bbce14e553"
                );

        Assertions.assertThat(result)
                .extracting(e ->
                        e.getParentMessage() != null ?
                                e.getParentMessage().getId()
                                : null)
                .containsExactly(
                        chatMessageId,
                        null
                );

        Assertions.assertThat(
                chatRoomRepository.findAll().get(0)
                        .getLastMessagedAt().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(result.get(0).getCreatedAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("채팅메시지 생성 후, 이벤트 로그에 저장되는지 확인")
    void sendChatMessage_이벤트_저장_확인() {
        // given
        Long chatRoomId = chatRoomService.createChatRoom(
                CreateChatRoomCommand.of(
                        "f52b1459-7061-465b-b230-b5bbce14e553",
                        ChatRoom.RoomType.GROUP,
                        "테스트 채팅방1",
                        new ArrayList<>(
                                List.of(
                                        "46700624-2054-4df6-a9f4-10424a46eccd",
                                        "68e000ef-e717-4b1e-97f1-1baabafda524"
                                )
                        )
                )
        );

        entityManager.flush();
        entityManager.clear();

        SendChatMessageCommand command = SendChatMessageCommand.of(
                "f52b1459-7061-465b-b230-b5bbce14e553",
                chatRoomId,
                ChatMessage.MessageType.TEXT,
                "안녕하세요1",
                null
        );

        // when
        log.info("\n=== Test Start ===");

        Long chatMessageId = service.sendChatMessage(command);
        entityManager.flush();
        entityManager.clear();

        service.sendChatMessage(SendChatMessageCommand.of(
                "46700624-2054-4df6-a9f4-10424a46eccd",
                chatRoomId,
                ChatMessage.MessageType.TEXT,
                "안녕하세요2",
                chatMessageId
        ));
        entityManager.flush();
        entityManager.clear();

        log.info("\n=== Test End ===");

        // then
        List<EventLogJpaEntity> eventLogs = eventLogJpaRepository.findAll();

        Assertions.assertThat(eventLogs).hasSize(2);

        Assertions.assertThat(eventLogs)
                .extracting(EventLogJpaEntity::getEventType)
                .containsOnly(PubEventType.CHAT_MESSAGE_SENT);
    }

    @Test
    @DisplayName("페이징 방식으로 특정 채팅방의 및 정보 조회")
    void getChatMessages_페이징() {
        // given
        Long chatRoomId = chatRoomService.createChatRoom(
                CreateChatRoomCommand.of(
                        "f52b1459-7061-465b-b230-b5bbce14e553",
                        ChatRoom.RoomType.GROUP,
                        "테스트 채팅방1",
                        new ArrayList<>(
                                List.of(
                                        "46700624-2054-4df6-a9f4-10424a46eccd",
                                        "68e000ef-e717-4b1e-97f1-1baabafda524"
                                )
                        )
                )
        );
        entityManager.flush();
        entityManager.clear();

        Long chatMessageId1 = service.sendChatMessage(SendChatMessageCommand.of(
                "f52b1459-7061-465b-b230-b5bbce14e553",
                chatRoomId,
                ChatMessage.MessageType.TEXT,
                "안녕하세요1",
                null
        ));
        entityManager.flush();
        entityManager.clear();

        Long chatMessageId2 = service.sendChatMessage(SendChatMessageCommand.of(
                "46700624-2054-4df6-a9f4-10424a46eccd",
                chatRoomId,
                ChatMessage.MessageType.TEXT,
                "안녕하세요2",
                chatMessageId1
        ));
        entityManager.flush();
        entityManager.clear();

        Long chatMessageId3 = service.sendChatMessage(SendChatMessageCommand.of(
                "68e000ef-e717-4b1e-97f1-1baabafda524",
                chatRoomId,
                ChatMessage.MessageType.TEXT,
                "안녕하세요3",
                chatMessageId1
        ));
        entityManager.flush();
        entityManager.clear();

        // when
        StopWatch watch = new StopWatch();
        log.info("\n=== Test Start ===");
        watch.start("쿼리 최적화 후 채팅메시지 조회");

        ChatDto.ChatMessagesInfo result = service.getChatMessagesWithPaging(GetChatMessagesCommand.of(
                "46700624-2054-4df6-a9f4-10424a46eccd",
                chatRoomId,
                0 + "_" + 2
        ));
        entityManager.flush();
        entityManager.clear();

        watch.stop();
        log.info("\n=== Test End ===");
        System.out.println(watch.prettyPrint());

        // then
        Assertions.assertThat(result.getChatMessageInfos())
                .extracting(ChatDto.ChatMessageInfo::getContent)
                .containsExactly(
                        "안녕하세요3",
                        "안녕하세요2"
                );

        Assertions.assertThat(result.getLastReadMessageIdInfos().values())
                .containsOnlyNulls();

        Assertions.assertThat(chatParticipantRepository.findAllByParticipantId("46700624-2054-4df6-a9f4-10424a46eccd"))
                .extracting(ChatParticipantJpaEntity::getLastReadMessageId)
                .containsOnly(
                        chatMessageId3
                );
    }

    @Test
    @DisplayName("커서 방식으로 특정 채팅방의 및 정보 조회")
    void getChatMessages_커서() {
        // given
        Long chatRoomId = chatRoomService.createChatRoom(
                CreateChatRoomCommand.of(
                        "f52b1459-7061-465b-b230-b5bbce14e553",
                        ChatRoom.RoomType.GROUP,
                        "테스트 채팅방1",
                        new ArrayList<>(
                                List.of(
                                        "46700624-2054-4df6-a9f4-10424a46eccd",
                                        "68e000ef-e717-4b1e-97f1-1baabafda524"
                                )
                        )
                )
        );
        entityManager.flush();
        entityManager.clear();

        Long chatMessageId1 = service.sendChatMessage(SendChatMessageCommand.of(
                "f52b1459-7061-465b-b230-b5bbce14e553",
                chatRoomId,
                ChatMessage.MessageType.TEXT,
                "안녕하세요1",
                null
        ));
        entityManager.flush();
        entityManager.clear();

        Long chatMessageId2 = service.sendChatMessage(SendChatMessageCommand.of(
                "46700624-2054-4df6-a9f4-10424a46eccd",
                chatRoomId,
                ChatMessage.MessageType.TEXT,
                "안녕하세요2",
                chatMessageId1
        ));
        entityManager.flush();
        entityManager.clear();

        Long chatMessageId3 = service.sendChatMessage(SendChatMessageCommand.of(
                "68e000ef-e717-4b1e-97f1-1baabafda524",
                chatRoomId,
                ChatMessage.MessageType.TEXT,
                "안녕하세요3",
                chatMessageId1
        ));
        entityManager.flush();
        entityManager.clear();

        // when
        StopWatch watch = new StopWatch();
        log.info("\n=== Test Start ===");
        watch.start("쿼리 최적화 후 채팅메시지 조회");

        ChatDto.ChatMessagesInfo result = service.getChatMessagesWithCursor(GetChatMessagesCommand.of(
                "46700624-2054-4df6-a9f4-10424a46eccd",
                chatRoomId,
                LocalDateTime.now() + "__" + 2
        ));
        entityManager.flush();
        entityManager.clear();

        watch.stop();
        log.info("\n=== Test End ===");
        System.out.println(watch.prettyPrint());

        // then
        Assertions.assertThat(result.getChatMessageInfos())
                .extracting(ChatDto.ChatMessageInfo::getContent)
                .containsExactly(
                        "안녕하세요3",
                        "안녕하세요2"
                );

        Assertions.assertThat(result.getLastReadMessageIdInfos().values())
                .containsOnlyNulls();

        Assertions.assertThat(chatParticipantRepository.findAllByParticipantId("46700624-2054-4df6-a9f4-10424a46eccd"))
                .extracting(ChatParticipantJpaEntity::getLastReadMessageId)
                .containsOnly(
                        chatMessageId3
                );

        Assertions.assertThat(Long.parseLong(
                result.getNextCursor().split("_")[1]))
                .isEqualTo(chatMessageId2);
    }
}
