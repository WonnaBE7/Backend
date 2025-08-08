package com.wonnabe.product.service;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.dto.InsuranceRecommendationResponseDTO;

import java.util.Map;

public interface InsuranceRecommendationService {
    /**
     * 사용자에게 보험 상품을 추천합니다.
     *
     * @param userId 사용자의 고유 ID
     * @param topN 추천할 보험 상품의 개수
     * @return {@link InsuranceRecommendationResponseDTO} 추천된 보험 상품 목록
     */
    InsuranceRecommendationResponseDTO recommendInsurance(String userId, int topN);

    /**
     * 페르소나별 가중치를 반환합니다.
     *
     * @return 페르소나 ID를 키로 하고, 각 점수 항목에 대한 가중치 배열을 값으로 하는 맵
     */
    Map<Integer, double[]> getPersonaWeights();

    /**
     * 보험 상품의 매치 점수를 계산합니다.
     *
     * @param product 보험 상품 VO
     * @param weights 페르소나별 가중치 배열
     * @return 계산된 매치 점수
     */
    double calculateScore(InsuranceProductVO product, double[] weights);
}
