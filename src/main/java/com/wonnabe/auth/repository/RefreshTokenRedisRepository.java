package com.wonnabe.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(String userId, String refreshToken, long expirationSeconds) {
        redisTemplate.opsForValue().set(userId, refreshToken, Duration.ofSeconds(expirationSeconds));
    }

    public String get(String userId) {
        Object value = redisTemplate.opsForValue().get(userId);
        return value != null ? value.toString() : null;
    }

    public void delete(String userId) {
        redisTemplate.delete(userId);
    }
}
