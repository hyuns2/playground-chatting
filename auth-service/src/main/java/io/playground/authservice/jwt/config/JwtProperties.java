package io.playground.authservice.jwt.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.jwt")
@Getter
@AllArgsConstructor
public class JwtProperties {
    private final String secretKey;
    private final String grantType;

    private final Long accessExp;
    private final Long refreshExp;

    private final String tokenTypeClaim;
    private final String deviceIdClaim;
    private final String authoritiesClaim;
}
