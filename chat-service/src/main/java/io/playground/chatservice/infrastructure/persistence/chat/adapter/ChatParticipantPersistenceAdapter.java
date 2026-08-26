package io.playground.chatservice.infrastructure.persistence.chat.adapter;

import io.playground.chatservice.application.chat.port.ChatParticipantRepositoryPort;
import io.playground.chatservice.infrastructure.persistence.chat.dto.ChatFlatInfo;
import io.playground.chatservice.infrastructure.persistence.chat.entity.ChatParticipantJpaEntity;
import io.playground.chatservice.infrastructure.persistence.chat.repository.ChatParticipantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatParticipantPersistenceAdapter implements ChatParticipantRepositoryPort {
    private final ChatParticipantJpaRepository chatParticipantRepository;

    @Override
    public boolean existsByChatRoomIdAndParticipantId(Long chatRoomId, String participantId) {
        return chatParticipantRepository
                .existsByParticipantIdAndChatRoomId(participantId, chatRoomId);
    }

//    @Override
//    public List<ChatDto.ChatRoomInfo> findChatInfosByParticipantId(String participantId) {
//        List<ChatRoomJpaEntity> roomEntities = chatParticipantRepository
//                .findAllByParticipantId(participantId).stream()
//                .map(ChatParticipantJpaEntity::getChatRoom)
//                .toList();
//
//        List<ChatParticipantJpaEntity> participantEntities = chatParticipantRepository
//                .findAllWithChatRoomByChatRoomIdIn(
//                        roomEntities.stream()
//                                .map(ChatRoomJpaEntity::getId)
//                                .toList()
//                );
//
//        Map<Long, List<ChatParticipantJpaEntity>> groupedByChatRoom = participantEntities.stream()
//                .collect(
//                        Collectors.groupingBy(p -> p.getChatRoom().getId())
//                );
//
//        List<ChatDto.ChatRoomInfo> result = new ArrayList<>();
//        for (ChatRoomJpaEntity roomEntity : roomEntities) {
//            result.add(
//                    ChatDto.ChatRoomInfo.of(
//                            roomEntity.getId(),
//                            roomEntity.getType(),
//                            roomEntity.getName(),
//                            roomEntity.getLastMessagedAt(),
//                            groupedByChatRoom.get(roomEntity.getId()).stream()
//                                    .map(participant -> ChatDto.ParticipantInfo.of(
//                                            participant.getParticipantId(),
//                                            participant.getNickName(),
//                                            participant.isAdmin()
//                                    )).toList()
//                    )
//            );
//        }
//
//        return result;
//    }

    @Override
    public List<ChatFlatInfo> findChatInfosByParticipantId(String participantId) {
        return chatParticipantRepository
                .findChatFlatInfosByParticipantId(participantId);
    }

    @Override
    public Map<String, Long> findLastReadMessageIdInfosByChatRoomId(Long chatRoomId) {
        Map<String, Long> lastReadMessageIdInfos = new HashMap<>();

        for (ChatParticipantJpaEntity entity : chatParticipantRepository.findAllByChatRoomId(chatRoomId))
            lastReadMessageIdInfos.put(
                            entity.getParticipantId(),
                            entity.getLastReadMessageId()
            );

        return lastReadMessageIdInfos;
    }

    @Override
    public List<Long> findChatRoomIdsByParticipantId(String participantId) {
        return chatParticipantRepository.findAllByParticipantId(participantId).stream()
                .map(chatParticipantEntity -> chatParticipantEntity.getChatRoom().getId())
                .toList();
    }

    @Override
    public void updateLastReadMessageIdByParticipantId(Long lastReadMessageId, String participantId) {
        chatParticipantRepository.updateLastReadMessageIdByParticipantId(
                lastReadMessageId, participantId);
    }
}
