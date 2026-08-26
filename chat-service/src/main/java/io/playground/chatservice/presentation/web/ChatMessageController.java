package io.playground.chatservice.presentation.web;

import io.playground.chatservice.application.chat.command.GetChatMessagesCommand;
import io.playground.chatservice.application.chat.command.SendChatMessageCommand;
import io.playground.chatservice.application.chat.service.ChatMessageUsecase;
import io.playground.securitycore.core.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageUsecase chatMessageUsecase;

    @MessageMapping("/chat-room")
    public void sendChatMessage(@Valid @RequestBody ChatRequestDto.SendChatMessage dto) {
        chatMessageUsecase.sendChatMessage(
                SendChatMessageCommand.from(dto)
        );
    }

    @GetMapping("/messages-paging/{chatRoomId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ChatResponseDto.GetChatMessagesInfo> getChatMessagesWithPaging(@AuthenticationPrincipal AuthPrincipal authPrincipal,
                                                                                         @PathVariable Long chatRoomId,
                                                                                         @RequestParam String limit) {
        return ResponseEntity.ok(
                ChatResponseDto.GetChatMessagesInfo.from(
                        chatMessageUsecase.getChatMessagesWithPaging(
                                GetChatMessagesCommand.of(
                                        authPrincipal.userId(),
                                        chatRoomId,
                                        limit
                                )
                        )
                )
        );
    }

    @GetMapping("/messages-cursor/{chatRoomId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ChatResponseDto.GetChatMessagesInfo> getChatMessagesWithCursor(@AuthenticationPrincipal AuthPrincipal authPrincipal,
                                                                                         @PathVariable Long chatRoomId,
                                                                                         @RequestParam String limit) {
        return ResponseEntity.ok(
                ChatResponseDto.GetChatMessagesInfo.from(
                        chatMessageUsecase.getChatMessagesWithCursor(
                                GetChatMessagesCommand.of(
                                        authPrincipal.userId(),
                                        chatRoomId,
                                        limit
                                )
                        )
                )
        );
    }
}
