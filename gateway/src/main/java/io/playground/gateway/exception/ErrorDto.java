package io.playground.gateway.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorDto {
    private final String name;
    private final String code;
    private final String message;

    public static ErrorDto of(String name, String code, String message) {
        return new ErrorDto(name, code, message);
    }

    public static ErrorDto from(BusinessException exception) {
        return new ErrorDto(
                exception.getName(),
                exception.getCode(),
                exception.getMessage()
        );
    }
}
