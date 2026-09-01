package io.playground.chatservice.application.usecase;

import io.playground.chatservice.application.port.ChatParticipantPersistencePort;
import io.playground.chatservice.application.port.ChatRoomPersistencePort;
import io.playground.chatservice.application.port.UserViewQueryPort;
import io.playground.chatservice.domain.ChatParticipant;
import io.playground.chatservice.domain.ChatRoom;
import io.playground.chatservice.domain.UserView;
import io.playground.chatservice.exception.BusinessErrorCode;
import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.presentation.ChatRequestDto;
import io.playground.chatservice.presentation.ChatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final UserViewQueryPort userViewQueryPort;
    private final ChatRoomPersistencePort chatRoomPort;
    private final ChatParticipantPersistencePort chatParticipantPort;
    private final ObjectMapper objectMapper;

    @Transactional
    public Long createChatRoom(Long userId,
                               ChatRequestDto.CreateChatRoom dto) {
        if (
                !dto.participantIds()
                        .contains(userId)
        )
            dto.participantIds().add(userId);

        // ToDo: 채팅방 타입 + 친구 관계가 맞는지 검증 추가 필요
        List<UserView> userViews = userViewQueryPort
                .findAllByUserIds(dto.participantIds());
        if (userViews.size() != dto.participantIds().size())
            throw new BusinessException(
                    BusinessErrorCode.PARTICIPANT_NOT_FOUND,
                    objectMapper.writeValueAsString(
                            userViews.stream()
                                    .map(UserView::getUserId)
                                    .filter(id ->
                                            !dto.participantIds()
                                                    .contains(id)
                                    )
                                    .toList()
                    )
            );

        ChatRoom chatRoom = chatRoomPort.save(
                ChatRoom.of(
                        null,
                        dto.type(),
                        dto.name(),
                        null
                )
        );
        chatParticipantPort.saveAll(
                userViews.stream()
                        .map(userView ->
                                ChatParticipant.of(
                                        null,
                                        chatRoom.getId(),
                                        userView.getUserId(),
                                        userView.getNickname(),
                                        userView.getUserId()
                                                .equals(userId),
                                        null
                                )
                        )
                        .toList()
        );

        return chatRoom.getId();
    }

    @Transactional(readOnly = true)
    public List<ChatResponseDto.GetChatRoomInfo> getChatRoomInfos(Long userId,
                                                                  int page,
                                                                  int size) {
        List<ChatRoom> chatRooms = chatParticipantPort
                .findChatRoomsByParticipantId(userId, page, size);

        List<ChatParticipant> chatParticipants = chatParticipantPort
                .findAllByChatRoomIds(
                        chatRooms.stream()
                                .map(ChatRoom::getId)
                                .toList()
                );

        Map<Long, List<ChatResponseDto.ParticipantInfo>> participantInfosByChatRoomId = new HashMap<>();
        for (ChatParticipant chatParticipant : chatParticipants) {
            if (
                    !participantInfosByChatRoomId
                            .containsKey(chatParticipant.getChatRoomId())
            )
                participantInfosByChatRoomId.put(
                        chatParticipant.getChatRoomId(),
                        new ArrayList<>()
                );

            participantInfosByChatRoomId.get(
                    chatParticipant.getChatRoomId()
            ).add(
                    ChatResponseDto.ParticipantInfo.builder()
                            .id(chatParticipant.getParticipantId())
                            .nickname(chatParticipant.getNickname())
                            .isAdmin(chatParticipant.isAdmin())
                            .build()
            );
        }

        return chatRooms.stream()
                .map(chatRoom ->
                        ChatResponseDto.GetChatRoomInfo.from(
                                chatRoom,
                                participantInfosByChatRoomId
                                        .getOrDefault(
                                                chatRoom.getId(),
                                                List.of()
                                        )
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public void validateChatParticipant(Long chatRoomId,
                                        Long participantId) {
        if (
                !chatParticipantPort.existsByChatRoomIdAndParticipantId(
                        chatRoomId,
                        participantId
                )
        )
            throw new BusinessException(
                    BusinessErrorCode.INVALID_CHAT_PARTICIPANT
            );
    }
}
