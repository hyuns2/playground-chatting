package io.playground.authservice.presentation.controller;

import io.playground.authservice.application.AuthService;
import io.playground.authservice.presentation.dto.AuthRequestDto;
import io.playground.authservice.presentation.dto.AuthResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Auth Service is up and running!");
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@Valid @RequestBody AuthRequestDto.SignUp dto) {
        authService.signUp(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponseDto.SignIn> signIn(@Valid @RequestBody AuthRequestDto.SignIn dto) {
        return ResponseEntity.ok(authService.signIn(dto));
    }

    @PostMapping("/reissue")
    public ResponseEntity<AuthResponseDto.Reissue> reissue(@Valid @RequestBody AuthRequestDto.Reissue dto) {
        return ResponseEntity.ok(authService.reissueToken(dto));
    }
}
