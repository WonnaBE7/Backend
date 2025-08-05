package com.wonnabe.product.service;

import com.wonnabe.product.dto.InsuranceRecommendationResponseDTO;

public interface InsuranceRecommendationService {
    /**
     * 사용자에게 보험 상품을 추천합니다.
     *
     * @param userId 사용자의 고유 ID
     * @param topN 추천할 보험 상품의 개수
     * @return {@link InsuranceRecommendationResponseDTO} 추천된 보험 상품 목록
     */
    InsuranceRecommendationResponseDTO recommendInsurance(String userId, int topN);
}
