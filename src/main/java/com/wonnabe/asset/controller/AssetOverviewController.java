package com.wonnabe.asset.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.asset.service.AssetOverviewService;
import com.wonnabe.common.util.JsonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets/main")
public class AssetOverviewController {

    private final AssetOverviewService assetOverviewService;

    @GetMapping("/overview")
    public ResponseEntity<Object> getAssetOverview(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        return JsonResponse.ok("총자산 현황 조회 성공", assetOverviewService.getAssetOverview(userId));
    }
}

