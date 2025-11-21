package com.example.demo.api.service;

import com.example.demo.api.model.FilialEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtClock clock = new JwtClock();

    @Value("${app.security.jwt.secret}")
    private String secret;

    @Value("${app.security.jwt.expiration-millis:3600000}")
    private long expirationMillis;

    public String generateToken(FilialEntity filial) {
        Map<String, Object> claims = Map.of(
                "filialId", filial.getCodigoFilial(),
                "nome", filial.getNomeFilial()
        );
        return buildToken(claims, filial.getLogin());
    }

    public String extractLogin(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, String login) {
        String username = extractLogin(token);
        return username != null && username.equals(login) && !isTokenExpired(token);
    }

    public Instant extractExpirationInstant(String token) {
        return extractClaim(token, claims -> claims.getExpiration().toInstant());
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }

    private String buildToken(Map<String, Object> claims, String subject) {
        Date issuedAt = Date.from(clock.now());
        Date expiration = new Date(issuedAt.getTime() + expirationMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationInstant(token).isBefore(clock.now());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    private Key getSigningKey() {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("JWT secret is not configured (app.security.jwt.secret)");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Pequeno wrapper para facilitar testes no futuro.
     */
    static class JwtClock {
        Instant now() {
            return Instant.now();
        }
    }
}
