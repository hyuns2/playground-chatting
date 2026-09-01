package io.playground.chatservice.application.dto;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;

public class ChatQueryDto {
    @Builder
    public record ChatMessageCursor(
            Long id,
            Instant createdAt
    ) {
    }

    public record LastReadMessageInfo(
            Long chatParticipantId,
            Long participantId,
            Long lastReadMessageId
    ) {
    }
}
