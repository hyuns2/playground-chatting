package io.playground.authservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<CustomErrorDto> handleException(CustomException e) {
        CustomErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus().value())
                .body(new CustomErrorDto(errorCode));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<CustomErrorDto> handleException(Exception e) {
        e.printStackTrace();
        CustomErrorCode errorCode = CustomErrorCode.SERVER_ERROR;
        return ResponseEntity.status(errorCode.getHttpStatus().value())
                .body(new CustomErrorDto(errorCode));
    }
}
