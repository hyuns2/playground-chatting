package io.playground.chatservice.presentation;

import io.playground.chatservice.application.usecase.ChatRoomService;
import io.playground.securitycore.jwt.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @PostMapping("/room")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Long> createChatRoom(@AuthenticationPrincipal AuthPrincipal authPrincipal,
                                               @Valid @RequestBody ChatRequestDto.CreateChatRoom dto) {
        return ResponseEntity.ok(
                chatRoomService.createChatRoom(
                        authPrincipal.userId(),
                        dto
                )
        );
    }

    @GetMapping("/rooms")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<ChatResponseDto.GetChatRoomInfo>> getChatRoomInfos(@AuthenticationPrincipal AuthPrincipal authPrincipal,
                                                                                  @RequestParam int page,
                                                                                  @RequestParam int size) {
        return ResponseEntity.ok(
                chatRoomService.getChatRoomInfos(
                        authPrincipal.userId(),
                        page,
                        size
                )
        );
    }
}
