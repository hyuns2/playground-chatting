package io.playground.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof BusinessException e)
            return setResponse(
                    exchange.getResponse(),
                    ErrorDto.from(e),
                    e.getHttpStatus()
            );

        ex.printStackTrace();
        return setResponse(
                exchange.getResponse(),
                ErrorDto.of(ex.getClass().getName(), "FATAL-000", "An unexpected error occurred: " + ex.getMessage() + ""),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private Mono<Void> setResponse(ServerHttpResponse response, ErrorDto dto, HttpStatus status) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(dto);
        } catch (JsonProcessingException e) {
            bytes = fallback().getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer dataBuffer = response.bufferFactory()
                .wrap(bytes);
        return response.writeWith(Mono.just(dataBuffer));
    }

    private String fallback() {
        return """
                {
                    "name": "INTERNAL_SERVER_ERROR",
                    "code": "FATAL-000",
                    "message": "An unexpected error occurred."
                }
                """;
    }
}
