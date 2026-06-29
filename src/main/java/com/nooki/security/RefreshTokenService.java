package com.nooki.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate tokenStorage;
    private final JwtService jwtService;

    @Value("${spring.security.refresh-key-expiration}")
    private long REFRESH_EXPIRATION_TIME;

    private static final String REDIS_KEY_PREFIX = "refresh_token:";

    public void saveRefreshToken(UUID userId, String token) {
        String cacheKey = REDIS_KEY_PREFIX + userId.toString();

        if(tokenStorage.hasKey(cacheKey)) {
            return;
        }

        tokenStorage.opsForHash().put(
                cacheKey,
                token,
                Duration.ofMillis(REFRESH_EXPIRATION_TIME).toString());
    }

    public boolean isTokenValid(String token) {
        if(!jwtService.validateRefreshToken(token)) {
            return false;
        }

        UUID userId = jwtService.extractUserIdFromRefreshToken(token);
        String cacheKey = REDIS_KEY_PREFIX + userId.toString();
        String savedToken = tokenStorage.opsForHash().get(cacheKey, token).toString();

        return savedToken != null && savedToken.equals(token);
    }

    public void deleteById(UUID userId) {
        String cacheKey = REDIS_KEY_PREFIX + userId.toString();
        if(!tokenStorage.hasKey(cacheKey)) {
            throw new RuntimeException("Refresh token not found");
        }
        tokenStorage.delete(cacheKey);
    }
}
