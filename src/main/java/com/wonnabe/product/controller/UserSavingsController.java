package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO;
import com.wonnabe.product.dto.UserSavingsDetailResponseDto;
import com.wonnabe.product.service.SavingsRecommendationService;
import com.wonnabe.product.service.UserSavingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/savings") // URL 경로를 savings로 명확하게 변경
@RequiredArgsConstructor
@Log4j2
public class UserSavingsController {

    private final UserSavingsService userSavingsService;
    private final SavingsRecommendationService savingsRecommendationService;

    @GetMapping("/{productId}")
    public ResponseEntity<UserSavingsDetailResponseDto> getSavingsDetail(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal CustomUser customUser
            ) {

        String userId = customUser.getUser().getUserId();
        UserSavingsDetailResponseDto savingsDetail = userSavingsService.getSavingsDetail(userId, productId);

        if (savingsDetail == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(savingsDetail);
    }

    // 추천 로직을 위한 핸들러 추가
    @GetMapping("/recommend")
    public ResponseEntity<SavingsRecommendationResponseDTO> recommendSavings(
            @RequestParam(defaultValue = "5") int topN,
            @AuthenticationPrincipal CustomUser customUser) {

        String userId = customUser.getUser().getUserId();

        // 서비스 호출
        SavingsRecommendationResponseDTO recommendations = savingsRecommendationService.recommendSavings(userId, topN);

        return ResponseEntity.ok(recommendations);
    }
}
