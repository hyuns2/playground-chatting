package io.playground.chatservice.presentation.web;

import io.playground.chatservice.application.chat.command.CreateChatRoomCommand;
import io.playground.chatservice.application.chat.service.ChatRoomUsecase;
import io.playground.securitycore.core.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomUsecase chatRoomUsecase;

    @PostMapping("/room")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Long> createChatRoom(@AuthenticationPrincipal AuthPrincipal authPrincipal,
                                               @Valid @RequestBody ChatRequestDto.CreateChatRoom dto) {
        return ResponseEntity.ok(
                chatRoomUsecase.createChatRoom(
                        CreateChatRoomCommand.from(authPrincipal.userId(), dto)
                )
        );
    }

    @GetMapping("/rooms")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<ChatResponseDto.GetChatRoomInfo>> getChatRooms(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return ResponseEntity.ok(
                chatRoomUsecase.getChatRooms(authPrincipal.userId()).stream()
                        .map(ChatResponseDto.GetChatRoomInfo::from)
                        .toList()
        );
    }
}
