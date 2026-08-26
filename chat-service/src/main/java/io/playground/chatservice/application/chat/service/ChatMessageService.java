package io.playground.chatservice.application.chat.service;

import io.playground.chatservice.application.chat.command.GetChatMessagesCommand;
import io.playground.chatservice.application.chat.command.SendChatMessageCommand;
import io.playground.chatservice.application.chat.dto.ChatDto;
import io.playground.chatservice.application.chat.port.ChatMessageRepositoryPort;
import io.playground.chatservice.application.chat.port.ChatParticipantRepositoryPort;
import io.playground.chatservice.application.chat.port.ChatRoomRepositoryPort;
import io.playground.chatservice.application.chat.port.EventPublisherPort;
import io.playground.chatservice.application.eventstream.PubEventType;
import io.playground.chatservice.domain.chat.message.ChatMessage;
import io.playground.chatservice.domain.chat.message.ChatMessageSentEvent;
import io.playground.chatservice.exception.CustomErrorCode;
import io.playground.chatservice.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatMessageService implements ChatMessageUsecase {
    private final ChatRoomRepositoryPort chatRoomRepositoryPort;
    private final ChatParticipantRepositoryPort chatParticipantRepositoryPort;
    private final ChatMessageRepositoryPort chatMessageRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final static int DEFAULT_LIMIT_SIZE = 20;

    @Override
    @Transactional
    public Long sendChatMessage(SendChatMessageCommand command) {
        if (!chatParticipantRepositoryPort.existsByChatRoomIdAndParticipantId(
                command.chatRoomId(), command.senderId()
        ))
            throw new CustomException(CustomErrorCode.INVALID_CHAT_PARTICIPANT);

        if (command.parentMessageId() != null
                && !chatMessageRepositoryPort.existsByIdAndChatRoomId(
                command.parentMessageId(), command.chatRoomId()))
            throw new CustomException(CustomErrorCode.INVALID_PARENT_MESSAGE);

        Long chatMessageId = saveChatMessageAndUpdateInfo(command);

        eventPublisherPort.publish(
                PubEventType.CHAT_MESSAGE_SENT,
                command.chatRoomId().toString(),
                ChatMessageSentEvent.of(
                        chatMessageId,
                        command.chatRoomId(),
                        command.senderId(),
                        command.type(),
                        command.content(),
                        command.parentMessageId(),
                        command.createdAt()
                )
        );

        return chatMessageId;
    }

    private Long saveChatMessageAndUpdateInfo(SendChatMessageCommand command) {
        chatRoomRepositoryPort.updateLastMessageAtByChatRoomId(
                command.createdAt(),
                command.chatRoomId()
        );

        return chatMessageRepositoryPort.save(
                ChatMessage.of(
                        null,
                        command.chatRoomId(),
                        command.senderId(),
                        command.type(),
                        command.content(),
                        command.parentMessageId(),
                        command.createdAt()
                )
        );
    }

//    @Override
//    @Transactional
//    public ChatDto.ChatMessagesInfo getChatMessages(GetChatMessagesCommand command) {
//        if (!chatParticipantRepositoryPort.existsByChatRoomIdAndParticipantId(
//                command.chatRoomId(), command.userId()
//        ))
//            throw new CustomException(CustomErrorCode.INVALID_CHAT_PARTICIPANT);
//
//        Map<String, Long> lastReadMessageIdInfos = chatParticipantRepositoryPort
//                .findLastReadMessageIdInfosByChatRoomId(command.chatRoomId());
//        List<ChatDto.ChatMessageInfo> chatMessageInfos = chatMessageRepositoryPort
//                .findAllByChatRoomIdOrderByCreatedAtDesc(
//                        command.chatRoomId(),
//                        PageRequest.of(command.page(),
//                                command.size(),
//                                Sort.by(Sort.Direction.DESC, "createdAt")
//                        )
//                );
//
//        if (command.page() == 0 && !chatMessageInfos.isEmpty())
//            chatParticipantRepositoryPort.updateLastReadMessageIdByParticipantId(
//                    chatMessageInfos.get(0).getChatMessageId(),
//                    command.userId()
//            );
//
//        return ChatDto.ChatMessagesInfo.of(
//                lastReadMessageIdInfos, chatMessageInfos);
//    }

    @Override
    @Transactional
    public ChatDto.ChatMessagesInfo getChatMessagesWithPaging(GetChatMessagesCommand command) {
        Map<String, Long> lastReadMessageIdInfos = chatParticipantRepositoryPort
                .findLastReadMessageIdInfosByChatRoomId(command.chatRoomId());
        if (lastReadMessageIdInfos.isEmpty())
            throw new CustomException(CustomErrorCode.INVALID_CHAT_PARTICIPANT);

        String temp = command.limit().split("_")[0];
        int page = temp != null && !temp.isBlank() ?
                Integer.parseInt(temp) :
                0;
        temp = command.limit().split("_")[1];
        int size = temp != null && !temp.isBlank() ?
                Integer.parseInt(temp) :
                DEFAULT_LIMIT_SIZE;

        List<ChatDto.ChatMessageInfo> chatMessageInfos = chatMessageRepositoryPort
                .findPageByChatRoomIdOrderByCreatedAtDesc(
                        command.chatRoomId(),
                        PageRequest.of(page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt")
                        )
                );

        if (page == 0 && !chatMessageInfos.isEmpty())
            chatParticipantRepositoryPort.updateLastReadMessageIdByParticipantId(
                    chatMessageInfos.get(0).getChatMessageId(),
                    command.userId()
            );

        String nextCursor = null;
        if (!chatMessageInfos.isEmpty()) {
            ChatDto.ChatMessageInfo lastChatMessageInfo = chatMessageInfos.get(chatMessageInfos.size() - 1);
            nextCursor = lastChatMessageInfo.getCreatedAt() + "_" + lastChatMessageInfo.getChatMessageId();
        }

        return ChatDto.ChatMessagesInfo.of(
                lastReadMessageIdInfos,
                chatMessageInfos,
                nextCursor
        );
    }

    public ChatDto.ChatMessagesInfo getChatMessagesWithCursor(GetChatMessagesCommand command) {
        Map<String, Long> lastReadMessageIdInfos = chatParticipantRepositoryPort
                .findLastReadMessageIdInfosByChatRoomId(command.chatRoomId());
        if (lastReadMessageIdInfos.isEmpty())
            throw new CustomException(CustomErrorCode.INVALID_CHAT_PARTICIPANT);

        String temp = command.limit().split("_")[0];
        LocalDateTime createdAt = temp != null && !temp.isBlank() ?
                LocalDateTime.parse(temp) :
                LocalDateTime.now();
        temp = command.limit().split("_")[1];
        Long id = temp != null && !temp.isBlank() ?
                Long.parseLong(temp) :
                null;
        temp = command.limit().split("_")[2];
        int size = temp != null && !temp.isBlank() ?
                Integer.parseInt(temp) :
                DEFAULT_LIMIT_SIZE;

        List<ChatDto.ChatMessageInfo> chatMessageInfos = chatMessageRepositoryPort
                .findAllByCursor(
                        command.chatRoomId(),
                        createdAt,
                        id,
                        size
                );

        // Todo: 임시 구현 -> 분산 환경 고려 & 정확한 최신 메시지인지 확인 필요
        if (!chatMessageInfos.isEmpty() &&
                (lastReadMessageIdInfos.get(command.userId()) == null ||
                lastReadMessageIdInfos.get(command.userId()) < chatMessageInfos.get(0).getChatMessageId()))
            chatParticipantRepositoryPort.updateLastReadMessageIdByParticipantId(
                    chatMessageInfos.get(0).getChatMessageId(),
                    command.userId()
            );

        String nextCursor = null;
        if (!chatMessageInfos.isEmpty()) {
            ChatDto.ChatMessageInfo lastChatMessageInfo = chatMessageInfos.get(chatMessageInfos.size() - 1);
            nextCursor = lastChatMessageInfo.getCreatedAt() + "_" + lastChatMessageInfo.getChatMessageId();
        }

        return ChatDto.ChatMessagesInfo.of(
                lastReadMessageIdInfos,
                chatMessageInfos,
                nextCursor
        );
    }
}
