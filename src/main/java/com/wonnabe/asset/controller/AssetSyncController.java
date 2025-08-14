// src/main/java/com/wonnabe/asset/controller/AssetSyncController.java
package com.wonnabe.asset.controller;

import com.wonnabe.codef.service.AssetSyncOrchestrator;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
public class AssetSyncController {

    private final AssetSyncOrchestrator orchestrator;

    @Qualifier("assetSyncExecutor")
    private final ThreadPoolTaskExecutor assetSyncExecutor;

    /**
     * 새로고침 버튼이 눌렸을 때 백그라운드 동기화 트리거(비차단).
     * force=true 면 freshness(30분)를 무시하고 즉시 시도.
     */
    @PostMapping("/sync")
    public ResponseEntity<Object> triggerSync(@AuthenticationPrincipal CustomUser user,
                                              @RequestParam(defaultValue = "false") boolean force) {
        String userId = user.getUser().getUserId();
        var freshness = force ? Duration.ZERO : Duration.ofMinutes(30);

        assetSyncExecutor.execute(() -> {
            try {
                orchestrator.syncNow(userId, Duration.ofMinutes(5), freshness);
            } catch (Exception e) {
                log.warn("async asset sync failed - userId={}, err={}", userId, e.toString());
            }
        });

        return JsonResponse.accepted(
                "동기화를 시작했습니다.",
                Map.of("queued", true, "force", force)
        );
    }
}
