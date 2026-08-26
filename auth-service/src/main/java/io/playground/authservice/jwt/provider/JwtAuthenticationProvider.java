package io.playground.authservice.jwt.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.playground.authservice.exception.CustomErrorCode;
import io.playground.authservice.exception.CustomException;
import io.playground.authservice.jwt.config.JwtProperties;
import io.playground.authservice.jwt.model.CustomPrincipal;
import io.playground.authservice.jwt.model.type.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationProvider {
    private final JwtProperties jwtProperties;
    private final Key key;

    public Authentication getAuthentication(TokenType tokenType, String tokenWithGrantType) {
        String token = resolveToken(tokenWithGrantType);
        Claims claims = parseClaims(token);

        if (!claims.get(jwtProperties.getTokenTypeClaim()).equals(tokenType.getType()))
            throw new CustomException(CustomErrorCode.INVALID_TOKEN_TYPE);

        String userId = claims.getSubject();
        String deviceKey = (String) claims.get(jwtProperties.getDeviceIdClaim());
        String authoritiesString = (String) claims.get(jwtProperties.getAuthoritiesClaim());

        return new UsernamePasswordAuthenticationToken(
                CustomPrincipal.of(userId, deviceKey),
                null,
                Arrays.stream(authoritiesString.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
    }

    private String resolveToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith(jwtProperties.getGrantType()))
            return token.substring(7);
        else
            throw new CustomException(CustomErrorCode.INVALID_TOKEN);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.INVALID_TOKEN);
        }
    }
}
