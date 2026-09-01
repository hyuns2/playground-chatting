package io.playground.chatservice.infrastructure.jpa.adapter;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.application.port.ChatParticipantPersistencePort;
import io.playground.chatservice.domain.ChatParticipant;
import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.infrastructure.jpa.entity.ChatParticipantEntity;
import io.playground.chatservice.infrastructure.jpa.entity.ChatRoomEntity;
import io.playground.chatservice.infrastructure.jpa.repository.ChatParticipantRepository;
import io.playground.chatservice.infrastructure.jpa.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatParticipantPersistenceAdapter implements ChatParticipantPersistencePort {
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public boolean existsByChatRoomIdAndParticipantId(Long chatRoomId,
                                                      Long participantId) {
        return chatParticipantRepository
                .existsByChatRoomIdAndParticipantId(
                        chatRoomId,
                        participantId
                );
    }

    @Override
    public List<ChatRoom> findChatRoomsByParticipantId(Long participantId,
                                                       int page,
                                                       int size) {
        return chatParticipantRepository
                .findChatRoomsByParticipantId(
                        participantId,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "chatRoom.lastMessagedAt"
                                )
                        )
                )
                .map(ChatRoomEntity::toDomain)
                .toList();
    }

    @Override
    public List<ChatParticipant> findAllByChatRoomIds(List<Long> chatRoomIds) {
        return chatParticipantRepository
                .findAllByChatRoom_IdIn(chatRoomIds).stream()
                .map(ChatParticipantEntity::toDomain)
                .toList();
    }

    @Override
    public List<ChatQueryDto.LastReadMessageInfo> findLastReadMessageInfosByChatRoomId(Long chatRoomId) {
        return chatParticipantRepository
                .findLastReadMessageInfosByChatRoomId(chatRoomId);
    }

    @Override
    public void saveAll(List<ChatParticipant> chatParticipants) {
        chatParticipantRepository.saveAll(
                chatParticipants.stream()
                        .map(chatParticipant ->
                                ChatParticipantEntity.from(
                                        chatParticipant,
                                        chatRoomRepository.getReferenceById(
                                                chatParticipant.getChatRoomId()
                                        )
                                )
                        )
                        .toList()
        );
    }

    @Override
    public void updateLastReadMessageIdById(Long id,
                                            Long lastReadMessageId) {
        chatParticipantRepository
                .updateLastReadMessageIdById(
                        id,
                        lastReadMessageId
                );
    }
}
