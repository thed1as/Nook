package com.library.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${spring.security.secret-access-key}")
    private String SECRET_ACCESS_KEY;
    @Value("${spring.security.secret-refresh-key}")
    private String SECRET_REFRESH_KEY;

    @Value("${spring.security.access-key-expiration}")
    private long ACCESS_EXPIRATION_TIME;
    @Value("${spring.security.refresh-key-expiration}")
    private long REFRESH_EXPIRATION_TIME;

    private final StringRedisTemplate tokenStorage;

    private Key getAccessSignKey() {
        return Keys.hmacShaKeyFor(SECRET_ACCESS_KEY.getBytes());
    }

    private Key getRefreshSignKey() {
        return Keys.hmacShaKeyFor(SECRET_REFRESH_KEY.getBytes());
    }

    public String generateAccessToken(UUID userId, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_TIME))
                .signWith(getAccessSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .signWith(getRefreshSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getRefreshSignKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID extractUserIdFromRefreshToken(String token) {
        String subject = Jwts.parserBuilder()
                .setSigningKey(getRefreshSignKey())
                .build().parseClaimsJws(token)
                .getBody().getSubject();
        return UUID.fromString(subject);
    }
    public UUID extractUserId(Claims claims) {
        String subject = claims.getSubject();
        if(subject == null || "null".equals(subject)) {
            throw new IllegalStateException("subject is null");
        }
        return UUID.fromString(subject);
    }

    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getAccessSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void blackListToken(String authHeader) {
        if(authHeader == null && !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("authHeader is null");
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            long remainingTime = expiration.getTime() - System.currentTimeMillis();

            if(remainingTime > 0) {
                String cacheKey = "blacklist:" + token;
                tokenStorage.opsForValue().set(cacheKey, "revoked", Duration.ofMillis(remainingTime));
            }
        } catch (Exception ignored) {}
    }

    public boolean isTokenBlackListed(String token) {
        return Boolean.TRUE.equals(tokenStorage.hasKey("blacklist:" + token));
    }
}
