package io.playground.securitycore.security;

import io.playground.securitycore.core.JwtAuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AutoConfiguration
@EnableMethodSecurity
public class SecurityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @Primary
    public JwtAuthenticationProvider jwtAuthenticationProvider(
            @Value("${auth.jwt.secret-key}") String secretKey,
            @Value("${auth.jwt.grant-type}") String grantType,
            @Value("${auth.jwt.device-id-claim}") String deviceIdClaim,
            @Value("${auth.jwt.authorities-claim}") String authoritiesClaim
    ) {
        return new JwtAuthenticationProvider(secretKey, grantType, deviceIdClaim, authoritiesClaim);
    }

    @Bean
    @ConditionalOnMissingBean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtAuthenticationProvider jwtAuthenticationProvider) {
        return new JwtAuthenticationFilter(jwtAuthenticationProvider);
    }

    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        httpSecurity
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        httpSecurity
                .securityMatcher("/**")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
