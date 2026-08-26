package io.playground.gateway.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private final String name;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
