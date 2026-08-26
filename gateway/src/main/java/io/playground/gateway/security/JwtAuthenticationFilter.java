package io.playground.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.PathContainer;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {
    private final JwtValidator jwtValidator;
    private final PathPatternParser parser = new PathPatternParser();
    public final static String[] excludedPaths = new String[]{
            "/auth/**",
            "/chat/*.html",
            "/chat/stomp"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String currentPath = exchange.getRequest().getURI().getPath();
        for (String path : excludedPaths) {
            if (parser.parse(path).matches(
                    PathContainer.parsePath(currentPath)))
                return chain.filter(exchange);
        }

        jwtValidator.validate(
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(HttpHeaders.AUTHORIZATION)
        );

        return chain.filter(exchange);
    }
}
