package com.wonnabe.asset.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.asset.service.AssetOverviewService;
import com.wonnabe.common.util.JsonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
public class AssetOverviewController {

    private static final Set<String> VALID_ASSET_CATEGORIES =
            Set.of("checking", "savings", "investment", "insurance", "pension", "other");

    private final AssetOverviewService assetOverviewService;

    //메인페이지 - 총자산 현황
    @GetMapping("/main/overview")
    public ResponseEntity<Object> getAssetOverview(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        return JsonResponse.ok("총자산 현황 조회 성공", assetOverviewService.getAssetOverview(userId));
    }

    //총자산페이지 - 총자산 카테고리 비율(입출금, 저축, 투자, 보험, 기타)
    @GetMapping("/categories")
    public ResponseEntity<Object> getAssetCategoryRatio(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        return JsonResponse.ok("총자산 카테고리 비율 조회 성공", assetOverviewService.getAssetCategoryRatio(userId));
    }

    //총자산페이지 - 자산 상세 내역
    @GetMapping("/detail")
    public ResponseEntity<Object> getAssetDetail(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        return JsonResponse.ok("자산 상세 내역 조회 성공", assetOverviewService.getAssetCategoryDetails(userId));
    }

    //총자산 상세페이지 - 카테고리별 계좌
    @GetMapping("/detail/assetCategory")
    public ResponseEntity<Object> getCategoryDetail(@AuthenticationPrincipal CustomUser customUser,
                                                    @RequestParam("assetCategory") String assetCategory) {
        String userId = customUser.getUser().getUserId();

        if (assetCategory == null || assetCategory.isBlank()) {
            throw new IllegalArgumentException("assetCategory는 필수입니다.");
        }
        if (!VALID_ASSET_CATEGORIES.contains(assetCategory)) {
            throw new IllegalArgumentException("유효하지 않은 자산 카테고리입니다: " + assetCategory);
        }

        Map<String, Object> result = assetOverviewService.getAccountDetailByCategory(userId, assetCategory);
        return JsonResponse.ok("자산 카테고리 상세 조회 성공", result);
    }

    // 총자산 상세페이지 -카테고리별 보유계좌 거래 내역
    @GetMapping("/detail/accountId")
    public ResponseEntity<Object> getAccountTransactions(@AuthenticationPrincipal CustomUser customUser,
                                                         @RequestParam("accountId") Long accountId) {
        String userId = customUser.getUser().getUserId();

        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 accountId 입니다.");
        }

        Map<String, Object> result = assetOverviewService.getAccountTransactionsById(userId, accountId);
        return JsonResponse.ok("카테고리별 보유계좌 거래 내역 조회 성공", result);
    }

}

