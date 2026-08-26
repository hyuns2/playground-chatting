package io.playground.authservice.presentation.dto;

import io.playground.authservice.domain.entity.type.DeviceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequestDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUp {
        @NotBlank
        @Email
        String email;

        @NotBlank
        String password;

        @NotBlank
        String name;

        @NotNull
        boolean pushAgree;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignIn {
        @NotBlank
        @Email
        String email;

        @NotBlank
        String password;

        @Valid
        DeviceInfo deviceInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfo {
        String deviceId;

        DeviceType deviceType;

        String deviceName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reissue {
        @NotBlank
        String refreshToken;
    }
}
