package io.playground.chatservice.application.usecase;

import io.playground.chatservice.application.dto.ChatQueryDto;
import io.playground.chatservice.application.event.ChatEventDto;
import io.playground.chatservice.application.event.EventProducerPort;
import io.playground.chatservice.application.port.ChatMessagePersistencePort;
import io.playground.chatservice.application.port.ChatParticipantPersistencePort;
import io.playground.chatservice.application.port.ChatRoomPersistencePort;
import io.playground.chatservice.domain.ChatMessage;
import io.playground.chatservice.exception.BusinessErrorCode;
import io.playground.chatservice.exception.BusinessException;
import io.playground.chatservice.presentation.ChatRequestDto;
import io.playground.chatservice.presentation.ChatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatRoomPersistencePort chatRoomPort;
    private final ChatParticipantPersistencePort chatParticipantPort;
    private final ChatMessagePersistencePort chatMessagePort;
    private final EventProducerPort eventProducerPort;

    @Transactional
    public Long sendChatMessage(Long senderId,
                                Long chatRoomId,
                                ChatRequestDto.SendChatMessage dto) {
        if (
                !chatParticipantPort.existsByChatRoomIdAndParticipantId(
                        chatRoomId, senderId
                )
        )
            throw new BusinessException(
                    BusinessErrorCode.INVALID_CHAT_PARTICIPANT
            );
        if (
                dto.parentMessageId() != null &&
                        !chatMessagePort.existsByIdAndChatRoomId(
                                dto.parentMessageId(), chatRoomId
                        )
        )
            throw new BusinessException(
                    BusinessErrorCode.INVALID_PARENT_MESSAGE
            );

        ChatMessage chatMessage = chatMessagePort.save(
                ChatMessage.of(
                        null,
                        chatRoomId,
                        senderId,
                        dto.type(),
                        dto.content(),
                        dto.parentMessageId(),
                        null
                )
        );
        chatRoomPort.updateLastMessagedAtByChatRoomId(
                chatRoomId,
                chatMessage.getCreatedAt()
        );

        eventProducerPort.produce(
                UUID.randomUUID().toString(),
                ChatEventDto.EventType.CHAT_MESSAGE_SENT,
                Instant.now(),
                ChatEventDto.ChatMessageSent.from(chatMessage)
        );
        return chatMessage.getId();
    }

    @Transactional
    public ChatResponseDto.GetChatMessagesInfo getChatMessagesInfo(Long userId,
                                                                   Long chatRoomId,
                                                                   int size,
                                                                   ChatRequestDto.ChatMessageCursor dto) {
        List<ChatQueryDto.LastReadMessageInfo> lastReadMessageInfos = chatParticipantPort
                .findLastReadMessageInfosByChatRoomId(chatRoomId);
        ChatQueryDto.LastReadMessageInfo currentUserLastReadInfo =
                lastReadMessageInfos.stream()
                        .filter(info ->
                                info.participantId()
                                        .equals(userId)
                        )
                        .findFirst()
                        .orElseThrow(
                                () -> new BusinessException(
                                        BusinessErrorCode.INVALID_CHAT_PARTICIPANT
                                )
                        );

        List<ChatResponseDto.ChatMessageInfo> chatMessageInfos = chatMessagePort
                .findAllByChatRoomIdAndCursor(
                        chatRoomId,
                        size,
                        dto != null ?
                                dto.toQueryDto() :
                                ChatQueryDto.ChatMessageCursor.builder()
                                        .id(null)
                                        .createdAt(null)
                                        .build()
                ).stream()
                .map(ChatResponseDto.ChatMessageInfo::from)
                .toList();

        if (
                !chatMessageInfos.isEmpty() &&
                (
                        currentUserLastReadInfo.lastReadMessageId() ==
                                null ||
                        currentUserLastReadInfo.lastReadMessageId() <
                                chatMessageInfos.getFirst().id()
                )
        )
            chatParticipantPort.updateLastReadMessageIdById(
                    currentUserLastReadInfo.chatParticipantId(),
                    chatMessageInfos.getFirst().id()
            );

        return ChatResponseDto.GetChatMessagesInfo.builder()
                .lastReadMessageInfos(
                        lastReadMessageInfos.stream()
                                .map(ChatResponseDto.LastReadMessageInfo::from)
                                .toList()
                )
                .chatMessageInfos(chatMessageInfos)
                .nextCursor(
                        chatMessageInfos.isEmpty() ?
                                null :
                                ChatResponseDto.NextChatMessageCursor.builder()
                                        .id(chatMessageInfos.getLast().id())
                                        .createdAt(chatMessageInfos.getLast().createdAt())
                                        .build()
                )
                .build();
    }
}
