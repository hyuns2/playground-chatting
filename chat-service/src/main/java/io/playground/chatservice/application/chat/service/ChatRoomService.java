package io.playground.chatservice.application.chat.service;

import io.playground.chatservice.application.chat.command.CreateChatRoomCommand;
import io.playground.chatservice.application.chat.dto.ChatDto;
import io.playground.chatservice.application.chat.port.ChatParticipantRepositoryPort;
import io.playground.chatservice.application.chat.port.ChatRoomRepositoryPort;
import io.playground.chatservice.application.read.model.UserView;
import io.playground.chatservice.application.read.port.UserViewQueryPort;
import io.playground.chatservice.domain.chat.ChatParticipant;
import io.playground.chatservice.domain.chat.room.ChatRoom;
import io.playground.chatservice.exception.CustomErrorCode;
import io.playground.chatservice.exception.CustomException;
import io.playground.chatservice.infrastructure.persistence.chat.dto.ChatFlatInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatRoomService implements ChatRoomUsecase {
    private final UserViewQueryPort userViewQueryPort;
    private final ChatRoomRepositoryPort chatRoomRepositoryPort;
    private final ChatParticipantRepositoryPort chatParticipantRepositoryPort;

    @Override
    @Transactional
    public Long createChatRoom(CreateChatRoomCommand command) {
        if (!command.participantIds().contains(command.creatorId()))
            command.participantIds().add(command.creatorId());

        List<UserView> userViews = userViewQueryPort
                .findAllByIds(command.participantIds());
        if (userViews.size() != command.participantIds().size())
            throw new CustomException(CustomErrorCode.PARTICIPANT_NOT_FOUND);

        // Todo: 추후 채팅방 생성시점도 request에서 받도록 변경
        ChatRoom chatRoom = ChatRoom.of(
                null,
                command.type(),
                command.name(),
                LocalDateTime.now()
        );
        List<ChatParticipant> chatParticipants = new ArrayList<>();
        for (UserView userView : userViews) {
            chatParticipants.add(
                    ChatParticipant.of(
                            null,
                            null,
                            userView.getUserId(),
                            userView.getNickName(),
                            userView.getUserId().equals(command.creatorId()),
                            null
                    )
            );
        }
        return chatRoomRepositoryPort.saveChatRoomWithParticipants(chatRoom, chatParticipants);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatDto.ChatRoomInfo> getChatRooms(String userId) {
        List<ChatFlatInfo> flatInfos = chatParticipantRepositoryPort
                .findChatInfosByParticipantId(userId);

        Map<Long, ChatDto.ChatRoomInfo> result = new HashMap<>();
        for (ChatFlatInfo flatInfo: flatInfos) {
            if (!result.containsKey(flatInfo.chatRoomId()))
                result.put(
                        flatInfo.chatRoomId(),
                        ChatDto.ChatRoomInfo.of(
                                flatInfo.chatRoomId(),
                                flatInfo.type(),
                                flatInfo.name(),
                                flatInfo.lastMessagedAt()
                        )
                );

            result.get(flatInfo.chatRoomId())
                    .addParticipantInfo(
                            ChatDto.ParticipantInfo.of(
                                    flatInfo.participantId(),
                                    flatInfo.nickName(),
                                    flatInfo.isAdmin()
                            )
                    );
        }

        return result.values().stream()
                .sorted(Comparator.comparing(ChatDto.ChatRoomInfo::getLastMessagedAt).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getChatRoomIds(String userId) {
        return chatParticipantRepositoryPort.findChatRoomIdsByParticipantId(userId);
    }
}
