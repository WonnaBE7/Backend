// package: com.wonnabe.codef.service
package com.wonnabe.codef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class AssetSyncOrchestrator {

    private final CodefAuthService codefAuthService;
    private final AssetSyncService assetSyncService;
    private final ObjectProvider<StringRedisTemplate> redisProvider;

    private static final String LOCK_KEY = "asset:sync:lock:%s";
    private static final String LAST_OK_KEY = "asset:sync:last:%s";

    private boolean acquireLock(String userId, Duration ttl) {
        var redis = redisProvider.getIfAvailable();
        if (redis == null) return true;
        return Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(LOCK_KEY.formatted(userId), "1", ttl)
        );
    }

    private void releaseLock(String userId) {
        var redis = redisProvider.getIfAvailable();
        if (redis != null) redis.delete(LOCK_KEY.formatted(userId));
    }

    private boolean isFresh(String userId, Duration freshness) {
        var redis = redisProvider.getIfAvailable();
        if (redis == null) return false;
        String ts = redis.opsForValue().get(LAST_OK_KEY.formatted(userId));
        if (ts == null) return false;
        return System.currentTimeMillis() - Long.parseLong(ts) < freshness.toMillis();
    }

    private void markLastSuccess(String userId) {
        var redis = redisProvider.getIfAvailable();
        if (redis != null) redis.opsForValue()
                .set(LAST_OK_KEY.formatted(userId), String.valueOf(System.currentTimeMillis()));
    }

    public void syncNow(String userId, Duration hardTimeout, Duration freshness) {
        // 1) 최근 성공이 30분 이내면 즉시 스킵 (쿨다운 정책이 필요할 때만)
        if (isFresh(userId, freshness)) {
            log.info("⏭️ fresh data, skip sync - userId={}", userId);
            return;
        }
        // 2) 원자적 락
        if (!acquireLock(userId, Duration.ofMinutes(5))) {
            // 다른 프로세스가 수행 중 → 대기하거나 바로 리턴(정책 선택)
            log.info("↻ sync in progress, skip join - userId={}", userId);
            return;
        }
        try {
            codefAuthService.syncUserCodef(userId);
            assetSyncService.syncAllAssets(userId); // 내부에서 join()
            markLastSuccess(userId);
        } finally {
            releaseLock(userId);
        }
    }

    /**
     * 로그인 시에 쓰는 '소프트 타임박스' 동기화.
     * - softTimeout 내에 끝나면 true
     * - 실패/시간초과면 false (작업은 백그라운드에서 계속 진행)
     */
    public boolean syncWithSoftTimeout(String userId, Duration softTimeout) {
        // 기본 하드 타임아웃/신선도 정책 (필요하면 오버로드로 조절)
        return syncWithSoftTimeout(
                userId,
                softTimeout,
                Duration.ofMinutes(5),    // hardTimeout: 락 대기/전체 동기화 허용 한도
                Duration.ofMinutes(30)    // freshness : 최근 성공 30분 이내면 스킵
        );
    }

    /**
     * 소프트 타임박스 + 하드 타임아웃/신선도 파라미터 지정 버전.
     */
    public boolean syncWithSoftTimeout(String userId,
                                       Duration softTimeout,
                                       Duration hardTimeout,
                                       Duration freshness) {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                // 이 호출은 내부에서 끝날 때까지 블로킹(AssetSyncService가 join() 보장)
                syncNow(userId, hardTimeout, freshness);
                return true;
            } catch (Exception e) {
                log.warn("asset sync failed (non-blocking login) - userId={}, err={}", userId, e.toString());
                return false;
            }
        }); // 별도 executor 미지정 시 common pool 사용(필요시 오케스트레이터용 풀 주입해 사용)

        try {
            return future.get(softTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception timeoutOrOther) {
            // ⏱️ 소프트 타임아웃 초과: 로그인은 진행, 작업은 백그라운드에서 계속
            log.info("asset sync soft-timeout → continue in background - userId={}", userId);
            return false;
        }
    }


}
