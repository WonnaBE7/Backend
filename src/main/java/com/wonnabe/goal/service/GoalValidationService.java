package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.GoalDetailResponseDTO;

public interface GoalValidationService {

    /**
     * 목표 상태값의 유효성 검증
     * 허용된 상태: "PUBLISHED"(발생됨)와 "ACHIEVED"(달성됨)
     *
     * @param status 검증할 목표 상태값
     */
    public void validateGoalStatus(String status);

    /**
     * 카테고리 ID에 해당하는 카테고리가 존재하는 지 검증
     * 데이터베이스에서 해당 ID로 카테고리를 조회하여 존재 여부를 확인
     *
     * @param categoryId 검증할 카테고리 ID
     */
    public void validateCategoryExists(Integer categoryId);

    /**
     * 특정 사용자의 목표가 존재하는지 검증하고 목표 상세 정보를 반환
     * 사용자 권한과 목표 존재 여부를 동시에 검증
     *
     * @param userId 목표 소유자의 사용자 ID
     * @param goalId 검증할 목표 ID
     * @return 목표 상세 정보
     */
    public GoalDetailResponseDTO validateGoalExists(String userId, Long goalId);

    /**
     * 특정 목표에 추천된 상품이 존재하는지 검증하고 상품 정보를 반환합니다.
     * 목표-상품 연관관계와 상품 존재 여부를 동시에 검증합니다.
     *
     * @param productId 검증할 상품 ID
     * @param goalId    목표 ID
     * @return 추천 상품 정보
     */
    public RecommendedProductVO validateProductExists(Long productId, Long goalId);

}
