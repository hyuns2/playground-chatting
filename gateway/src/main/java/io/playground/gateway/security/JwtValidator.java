package io.playground.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerErrorException;

public class JwtValidator {
    @Value("${auth.jwt.secret-key}")
    private String secretKey;
    @Value("${auth.jwt.grant-type}")
    private String grantType;
    @Value("${auth.jwt.token-type-claim}")
    private String tokenTypeClaim;
    @Value("${auth.jwt.authorities-claim}")
    private String authoritiesClaim;
    private static final String TOKEN_TYPE = "access";

    public void validate(String tokenWithGrantType) {
        String token = resolveToken(tokenWithGrantType);
        Claims claims = parseClaims(token);

        if (!claims.get(tokenTypeClaim).equals(TOKEN_TYPE))
            throw new IllegalArgumentException("Invalid token type");

        String authoritiesString = (String) claims.get(authoritiesClaim);
        if (authoritiesString.isBlank())
            throw new IllegalArgumentException("No authorities in token");
    }

    private String resolveToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith(grantType))
            return token.substring(7);
        else
            throw new IllegalArgumentException("No token or Invalid token grant type");
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(
                            Decoders.BASE64.decode(secretKey)
                    )).build()
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (Exception e) {
            throw new ServerErrorException("Failed to parse JWT token", e);
        }
    }
}
