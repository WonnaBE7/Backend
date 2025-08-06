package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.GoalCreateRequestDTO;

import java.util.List;

public interface ProductRecommendationService {

    /**
     * 사용자의 목표 조건에 맞는 최적의 금융상품 추천
     * 적금과 예금 상품을 모두 고려하여 종합적으로 분석해
     * 목표 달성에 가장 유리한 상품 선별
     *
     * @param request 목표 생성 요청 정보
     * @param goalId  목표 ID
     * @return 추천 상품 목록 (최대 5개)
     */
    public List<RecommendedProductVO> calculateRecommendations(GoalCreateRequestDTO request, Long goalId);
}
