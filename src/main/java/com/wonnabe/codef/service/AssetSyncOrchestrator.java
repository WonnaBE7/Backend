package com.wonnabe.codef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    /**
     * 사용자별 자산 동기화를 위해 분산 락을 획득합니다.
     * Redis가 사용 가능하면 TTL과 함께 SETNX로 락을 시도하고, 사용 불가 시에는 락을 건너뜁니다.
     *
     * @param userId 락을 걸 사용자 식별자
     * @param ttl    락 유지 시간(만료 시간)
     * @return 락 획득 성공 여부 (true: 성공, false: 이미 다른 동기화가 진행 중)
     */
    private boolean acquireLock(String userId, Duration ttl) {
        var redis = redisProvider.getIfAvailable();
        if (redis == null) return true;
        return Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(LOCK_KEY.formatted(userId), "1", ttl)
        );
    }

    /**
     * 사용자별 동기화 락을 해제합니다.
     * Redis가 사용 가능할 때만 실행되며, 키가 없으면 무시됩니다.
     *
     * @param userId 락을 해제할 사용자 식별자
     */
    private void releaseLock(String userId) {
        var redis = redisProvider.getIfAvailable();
        if (redis != null) redis.delete(LOCK_KEY.formatted(userId));
    }


    /**
     * 최근 동기화 성공 시각을 기준으로 "신선도"를 판단합니다.
     * Redis에 저장된 마지막 성공 타임스탬프가 freshness 이내이면 true를 반환합니다.
     *
     * @param userId    사용자 식별자
     * @param freshness 신선도로 인정할 최대 경과 시간
     * @return 신선하면 true, 아니면 false
     */
    private boolean isFresh(String userId, Duration freshness) {
        var redis = redisProvider.getIfAvailable();
        if (redis == null) return false;
        String ts = redis.opsForValue().get(LAST_OK_KEY.formatted(userId));
        if (ts == null) return false;
        return System.currentTimeMillis() - Long.parseLong(ts) < freshness.toMillis();
    }

    /**
     * 마지막 동기화 성공 시점을 Redis에 기록합니다.
     * 현재 epoch milli를 값으로 저장합니다.
     *
     * @param userId 사용자 식별자
     */
    private void markLastSuccess(String userId) {
        var redis = redisProvider.getIfAvailable();
        if (redis != null) redis.opsForValue()
                .set(LAST_OK_KEY.formatted(userId), String.valueOf(System.currentTimeMillis()));
    }


    /**
     * 자산 동기화를 즉시 수행합니다.
     * 1) 최근 성공이 freshness 이내면 동기화를 스킵합니다.
     * 2) 분산 락을 획득하지 못하면(다른 프로세스에서 실행 중) 스킵합니다.
     * 3) CODEF 인증을 최신화한 뒤, 기관별 동기화 로직을 실행하고 성공 시각을 기록합니다.
     *
     * @param userId     사용자 식별자
     * @param hardTimeout (미사용) 하드 타임아웃 파라미터(확장 여지; 현재 구현에서는 직접 적용하지 않음)
     * @param freshness  신선도 기준 시간(이내면 동기화 스킵)
     */
    public void syncNow(String userId, Duration hardTimeout, Duration freshness) {
        if (isFresh(userId, freshness)) {
            log.info("⏭️ fresh data, skip sync - userId={}", userId);
            return;
        }
        if (!acquireLock(userId, Duration.ofMinutes(5))) {
            log.info("↻ sync in progress, skip join - userId={}", userId);
            return;
        }
        try {
            codefAuthService.syncUserCodef(userId);
            assetSyncService.syncAllAssets(userId);
            markLastSuccess(userId);
        } finally {
            releaseLock(userId);
        }
    }


    /**
     * 로그인 시 사용하는 '소프트 타임박스' 동기화입니다.
     * - softTimeout 내에 작업이 끝나면 true
     * - 실패/시간초과 시 false (작업은 백그라운드에서 계속 진행)
     * 내부적으로 freshness=30분, 락 TTL=5분 기본값을 사용합니다.
     *
     * @param userId      사용자 식별자
     * @param softTimeout 로그인 흐름에서 기다릴 최대 시간
     * @return 소프트 타임아웃 내 완료 여부(true/false)
     */
    public boolean syncWithSoftTimeout(String userId, Duration softTimeout) {
        return syncWithSoftTimeout(
                userId,
                softTimeout,
                Duration.ofMinutes(5),
                Duration.ofMinutes(30)
        );
    }

    /**
     * 소프트 타임아웃 + 파라미터 지정 버전의 동기화입니다.
     * 동기화 로직을 별도 스레드에서 실행하고, softTimeout 동안만 결과를 대기합니다.
     * 시간 내 완료되면 true, 타임아웃/예외 시 false를 반환합니다.
     *
     * @param userId      사용자 식별자
     * @param softTimeout 대기할 최대 시간
     * @param hardTimeout (미사용) 하드 타임아웃 파라미터(확장 여지)
     * @param freshness   신선도 기준 시간(이내면 동기화 스킵)
     * @return 소프트 타임아웃 내 완료 여부(true/false)
     */
    public boolean syncWithSoftTimeout(String userId,
                                       Duration softTimeout,
                                       Duration hardTimeout,
                                       Duration freshness) {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                syncNow(userId, hardTimeout, freshness);
                return true;
            } catch (Exception e) {
                log.warn("asset sync failed (non-blocking login) - userId={}, err={}", userId, e.toString());
                return false;
            }
        });
        try {
            return future.get(softTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception timeoutOrOther) {
            log.info("asset sync soft-timeout → continue in background - userId={}", userId);
            return false;
        }
    }
}
