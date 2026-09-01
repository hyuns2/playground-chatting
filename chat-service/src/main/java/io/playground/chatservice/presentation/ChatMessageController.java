package io.playground.chatservice.presentation;

import io.playground.chatservice.application.usecase.ChatMessageService;
import io.playground.securitycore.jwt.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @MessageMapping("/room/{chatRoomId}")
    public Long sendChatMessage(Authentication authentication,
                                @DestinationVariable Long chatRoomId,
                                @Valid ChatRequestDto.SendChatMessage dto) {
        AuthPrincipal authPrincipal = (AuthPrincipal) authentication.getPrincipal();

        return chatMessageService.sendChatMessage(
                authPrincipal.userId(),
                chatRoomId,
                dto
        );
    }

    @PostMapping("/room/{chatRoomId}/messages/cursor")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ChatResponseDto.GetChatMessagesInfo> getChatMessagesInfo(@AuthenticationPrincipal AuthPrincipal authPrincipal,
                                                                                   @PathVariable Long chatRoomId,
                                                                                   @RequestParam int size,
                                                                                   @Valid @RequestBody(required = false) ChatRequestDto.ChatMessageCursor dto) {
        return ResponseEntity.ok(
                chatMessageService.getChatMessagesInfo(
                        authPrincipal.userId(),
                        chatRoomId,
                        size,
                        dto
                )
        );
    }
}
