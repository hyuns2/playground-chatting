package io.playground.authservice.jwt.provider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.playground.authservice.exception.CustomErrorCode;
import io.playground.authservice.exception.CustomException;
import io.playground.authservice.jwt.config.JwtProperties;
import io.playground.authservice.jwt.model.CustomPrincipal;
import io.playground.authservice.jwt.model.JwtToken;
import io.playground.authservice.jwt.model.type.TokenType;
import io.playground.authservice.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private final Key key;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    public JwtToken generateToken(CustomPrincipal customPrincipal, List<SimpleGrantedAuthority> authorities) {
        String authoritiesToString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(String::toString)
                .collect(Collectors.joining(","));

        String accessToken = Jwts.builder()
                .setSubject(customPrincipal.getUserId())
                .claim(jwtProperties.getTokenTypeClaim(), TokenType.ACCESS.getType())
                .claim(jwtProperties.getDeviceIdClaim(), customPrincipal.getDeviceId())
                .claim(jwtProperties.getAuthoritiesClaim(), authoritiesToString)
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessExp()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(customPrincipal.getUserId())
                .claim(jwtProperties.getTokenTypeClaim(), TokenType.REFRESH.getType())
                .claim(jwtProperties.getDeviceIdClaim(), customPrincipal.getDeviceId())
                .claim(jwtProperties.getAuthoritiesClaim(), authoritiesToString)
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExp()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        refreshTokenRepository.saveOrUpdate(
                customPrincipal.getUserId(),
                customPrincipal.getDeviceId(),
                refreshToken
        );

        return JwtToken.of(jwtProperties.getGrantType(), accessToken, refreshToken);
    }

    public CustomPrincipal validateRefreshToken(String refreshToken) {
        CustomPrincipal customPrincipal = (CustomPrincipal) jwtAuthenticationProvider
                .getAuthentication(TokenType.REFRESH, refreshToken).getPrincipal();

        String savedRefreshToken = refreshTokenRepository
                .findByUserIdAndDeviceKey(customPrincipal.getUserId(), customPrincipal.getDeviceId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_TOKEN));
        if (!refreshToken.equals(savedRefreshToken))
            throw new CustomException(CustomErrorCode.INVALID_TOKEN);

        return customPrincipal;
    }

    public void invalidateRefreshToken(CustomPrincipal customPrincipal) {
        refreshTokenRepository.delete(customPrincipal.getUserId(), customPrincipal.getDeviceId());
    }
}
