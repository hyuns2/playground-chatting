package io.playground.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomErrorDto {
    private final String name;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    public CustomErrorDto(CustomErrorCode errorCode) {
        this.name = errorCode.name();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
    }
}
