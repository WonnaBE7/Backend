package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO;
import com.wonnabe.product.service.CardService;
import com.wonnabe.product.service.SavingsRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations/savings")
@Log4j2
public class RecommendSavingsController{
        private final SavingsRecommendationService savingsRecommendationService;

        public RecommendSavingsController(@Qualifier("SavingsRecommendationServiceImpl") SavingsRecommendationService savingsRecommendationService) {
        this.savingsRecommendationService =savingsRecommendationService;
}


    @GetMapping
    public ResponseEntity<Object> recommendSavings(
            @RequestParam(defaultValue = "5") int topN,
            @AuthenticationPrincipal CustomUser customUser) {

        String userId = customUser.getUser().getUserId();

        SavingsRecommendationResponseDTO recommendations = savingsRecommendationService.recommendSavings(userId, topN);

        // jsonResponse 수정
        return JsonResponse.ok("성공적으로 추천 예적금상품을 반환하였습니다.",recommendations);
    }
}
