package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO;
import com.wonnabe.product.service.SavingsRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations/savings")
@RequiredArgsConstructor
@Log4j2
public class RecommendSavingsController {

    private final SavingsRecommendationService savingsRecommendationService;

    @GetMapping
    public ResponseEntity<SavingsRecommendationResponseDTO> recommendSavings(
            @RequestParam(defaultValue = "5") int topN,
            @AuthenticationPrincipal CustomUser customUser) {

        String userId = customUser.getUser().getUserId();

        SavingsRecommendationResponseDTO recommendations = savingsRecommendationService.recommendSavings(userId, topN);

        return ResponseEntity.ok(recommendations);
    }
}
