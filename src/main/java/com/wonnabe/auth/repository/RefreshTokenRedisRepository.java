package com.wonnabe.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * Redis를 통해 사용자별 Refresh Token을 저장, 조회, 삭제하는 리포지토리입니다.
 */
@Repository
@RequiredArgsConstructor
@RedisHash("refreshToken")
public class RefreshTokenRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 주어진 userId를 키로 하여 Refresh Token을 Redis에 저장합니다.
     * @param userId 사용자 고유 ID
     * @param refreshToken 저장할 Refresh Token 값
     * @param expirationSeconds 만료 시간 (초 단위)
     */
    public void save(String userId, String refreshToken, long expirationSeconds) {
        redisTemplate.opsForValue().set(userId, refreshToken, Duration.ofSeconds(expirationSeconds));
    }

    /**
     * 주어진 userId를 키로 하여 Redis에서 Refresh Token을 조회합니다.
     * @param userId 사용자 고유 ID
     * @return 저장된 Refresh Token (없으면 null)
     */
    public String get(String userId) {
        Object value = redisTemplate.opsForValue().get(userId);
        return value != null ? value.toString() : null;
    }

    /**
     * 주어진 userId에 해당하는 Refresh Token을 Redis에서 삭제합니다.
     * @param userId 사용자 고유 ID
     */
    public void delete(String userId) {
        redisTemplate.delete(userId);
    }
}
