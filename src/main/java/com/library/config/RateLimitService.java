package com.library.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private static final String RATE_LIMIT_KEY = "RATE_LIMIT:";

    private final RedisScript<Long> rateLimitScript = new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1]); " +
            "if current == 1 then " +
            "   redis.call('EXPIRE', KEYS[1], ARGV[1]);" +
            "end; " +
            "return current;",
            Long.class
    );

    public boolean isAllowed(String ip, String path, int limit, int WINDOW_SECONDS) {
        String cacheKey = RATE_LIMIT_KEY + ip + path;

        Long requests = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(cacheKey),
                String.valueOf(WINDOW_SECONDS)
        );

        return requests <= limit;
    }
}
