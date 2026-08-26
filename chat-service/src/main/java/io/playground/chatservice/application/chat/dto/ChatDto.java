package io.playground.chatservice.application.chat.dto;

import io.playground.chatservice.domain.chat.message.ChatMessage;
import io.playground.chatservice.domain.chat.message.ChatMessageSentEvent;
import io.playground.chatservice.domain.chat.room.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomInfo {
        Long chatRoomId;

        ChatRoom.RoomType type;

        String name;

        LocalDateTime lastMessagedAt;

        List<ParticipantInfo> participantInfos;

//        public static ChatRoomInfo of(Long chatRoomId,
//                                      ChatRoom.RoomType type,
//                                      String name,
//                                      LocalDateTime lastMessagedAt,
//                                      List<ParticipantInfo> participantInfos) {
//            return new ChatRoomInfo(chatRoomId,
//                    type,
//                    name,
//                    lastMessagedAt,
//                    participantInfos);
//        }

        public static ChatRoomInfo of(Long chatRoomId,
                                      ChatRoom.RoomType type,
                                      String name,
                                      LocalDateTime lastMessagedAt) {
            return new ChatRoomInfo(chatRoomId,
                    type,
                    name,
                    lastMessagedAt,
                    new ArrayList<>());
        }

        public void addParticipantInfo(ParticipantInfo participantInfo) {
            this.participantInfos.add(participantInfo);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        String id;

        String nickName;

        boolean isAdmin;

        public static ParticipantInfo of(String participantId, String nickName, boolean isAdmin) {
            return new ParticipantInfo(
                    participantId,
                    nickName,
                    isAdmin
            );
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ChatMessagesInfo {
        Map<String, Long> lastReadMessageIdInfos;

        List<ChatMessageInfo> chatMessageInfos;

        String nextCursor;

        public static ChatMessagesInfo of(Map<String, Long> lastReadMessageIdInfos,
                                          List<ChatMessageInfo> chatMessageInfos,
                                          String nextCursor) {
            return new ChatMessagesInfo(
                    lastReadMessageIdInfos,
                    chatMessageInfos,
                    nextCursor
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageInfo {
        Long chatMessageId;

        Long chatRoomId;

        String senderId;

        ChatMessage.MessageType type;

        String content;

        Long parentMessageId;

        LocalDateTime createdAt;

        public static ChatMessageInfo of(Long chatMessageId,
                                         Long chatRoomId,
                                         String senderId,
                                         ChatMessage.MessageType type,
                                         String content,
                                         Long parentMessageId,
                                         LocalDateTime createdAt) {
            return new ChatMessageInfo(
                    chatMessageId,
                    chatRoomId,
                    senderId,
                    type,
                    content,
                    parentMessageId,
                    createdAt
            );
        }

        public static ChatDto.ChatMessageInfo from(ChatMessageSentEvent event) {
            return new ChatDto.ChatMessageInfo(
                    event.chatMessageId(),
                    event.chatRoomId(),
                    event.senderId(),
                    event.type(),
                    event.content(),
                    event.parentMessageId(),
                    event.createdAt()
            );
        }
    }
}
