package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.product.dto.InsuranceRecommendationResponseDTO;
import com.wonnabe.product.service.InsuranceRecommendationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations/insurance")
@Log4j2
public class RecommendInsuranceController {

    private final InsuranceRecommendationService insuranceRecommendationService;

    public RecommendInsuranceController(@Qualifier("InsuranceRecommendationServiceImpl") InsuranceRecommendationService insuranceRecommendationService) {
        this.insuranceRecommendationService = insuranceRecommendationService;
    }

    /**
     * 사용자에게 보험 상품을 추천하는 API.
     *
     * @param topN 추천할 보험 상품의 개수
     * @param customUser Spring Security를 통해 주입되는 현재 사용자 정보
     * @return {@link InsuranceRecommendationResponseDTO} 추천된 보험 상품 목록
     */
    @GetMapping
    public ResponseEntity<InsuranceRecommendationResponseDTO> recommendInsurance(
            @RequestParam(defaultValue = "5") int topN,
            @AuthenticationPrincipal CustomUser customUser) {

        String userId = customUser.getUser().getUserId();

        InsuranceRecommendationResponseDTO recommendations = insuranceRecommendationService.recommendInsurance(userId, topN);

        return ResponseEntity.ok(recommendations);
    }
}
