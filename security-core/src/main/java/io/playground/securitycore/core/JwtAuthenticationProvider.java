package io.playground.securitycore.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.List;

public class JwtAuthenticationProvider {
    private final String secretKey;
    private final String grantType;
    private final String deviceIdClaim;
    private final String authoritiesClaim;

    public JwtAuthenticationProvider(String secretKey,
                                     String grantType,
                                     String deviceIdClaim,
                                     String authoritiesClaim) {
        this.secretKey = secretKey;
        this.grantType = grantType;
        this.deviceIdClaim = deviceIdClaim;
        this.authoritiesClaim = authoritiesClaim;
    }

    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        String prefix = grantType + " ";

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(prefix))
            return bearerToken.substring(prefix.length());

        return null;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String userId = claims.getSubject();
        String deviceId = (String) claims.get(deviceIdClaim);
        String authoritiesString = (String) claims.get(authoritiesClaim);

        return new UsernamePasswordAuthenticationToken(
                AuthPrincipal.of(userId, deviceId),
                null,
                authoritiesString.isBlank()
                        ? List.of()
                        : Arrays.stream(authoritiesString.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
    }

    private Claims parseClaims(String token) {
        Key key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretKey)
        );

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException expiredJwtException) {
            return expiredJwtException.getClaims();
        } catch (Exception e) {
            throw e;
        }
    }
}
