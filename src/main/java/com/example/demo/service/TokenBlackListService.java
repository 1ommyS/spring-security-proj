package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void blacklist(String tokenId, Date expiresAt) {
        if (isTokenValid(tokenId) || expiresAt == null) return;

        Duration ttl = Duration.between(Instant.now(), expiresAt.toInstant());

        if (ttl.isPositive()) {
            redisTemplate.opsForValue().set(key(tokenId), "1", ttl);
        }
    }

    public boolean isBlackListed(String tokenId) {
        if (isTokenValid(tokenId)) return false;

        return Boolean.TRUE.equals(redisTemplate.hasKey(key(tokenId)));
    }

    private static boolean isTokenValid(String tokenId) {
        return tokenId == null || tokenId.isBlank();
    }


    private String key(String tokenId) {
        return BLACKLIST_PREFIX + tokenId;
    }
}
