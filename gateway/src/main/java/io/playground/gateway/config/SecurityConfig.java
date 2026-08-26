package io.playground.gateway.config;

import io.playground.gateway.security.JwtAuthenticationFilter;
import io.playground.gateway.security.JwtValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public JwtValidator jwtValidator() {
        return new JwtValidator();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtValidator jwtValidator) {
        return new JwtAuthenticationFilter(jwtValidator);
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity serverHttpSecurity,
                                                      JwtAuthenticationFilter jwtAuthenticationFilter) {
        serverHttpSecurity
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable);

        return serverHttpSecurity
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
