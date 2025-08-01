package com.wonnabe.asset.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.asset.service.AssetOverviewService;
import com.wonnabe.common.util.JsonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
public class AssetOverviewController {

    private final AssetOverviewService assetOverviewService;

    @GetMapping("/main/overview")
    public ResponseEntity<Object> getAssetOverview(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        return JsonResponse.ok("총자산 현황 조회 성공", assetOverviewService.getAssetOverview(userId));
    }

    @GetMapping("/categories")
    public ResponseEntity<Object> getAssetCategoryRatio(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        return JsonResponse.ok("총자산 카테고리 비율 조회 성공", assetOverviewService.getAssetCategoryRatio(userId));
    }

    @GetMapping("/detail")
    public ResponseEntity<Object> getAssetDetail(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        return JsonResponse.ok("자산 상세 내역 조회 성공", assetOverviewService.getAssetCategoryDetails(userId));
    }

    @GetMapping("/detail/assetCategory")
    public ResponseEntity<Object> getCategoryDetail(@AuthenticationPrincipal CustomUser customUser,
                                                    @RequestParam("assetCategory") String assetCategory) {
        String userId = customUser.getUser().getUserId();

        if (!isValidAssetCategory(assetCategory)) {
            throw new IllegalArgumentException("잘못된 자산 카테고리입니다: " + assetCategory);
        }

        return JsonResponse.ok("자산 카테고리 상세 조회 성공",
                assetOverviewService.getAccountDetailByCategory(userId, assetCategory));
    }

    private boolean isValidAssetCategory(String category) {
        return List.of("checking", "savings", "investment", "insurance", "pension", "other").contains(category);
    }

}

