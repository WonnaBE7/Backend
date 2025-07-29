package com.wonnabe.goal.service;

import com.wonnabe.goal.dto.*;

import java.util.UUID;

public interface GoalService {

    /**
     * 사용자의 전체 목표 리스트 조회
     * @param userId 사용자 ID
     * @return 목표 리스트 요약 정보
     */
    public GoalListResponseDTO getGoalList(UUID userId);

    /**
     * 특정 목표의 상세 정보 조회
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     * @return 목표 상세 정보
     */
    public GoalDetailResponseDTO getGoalDetail(UUID userId, Long goalId);

    /**
     * 새로운 목표 생성
     * @param userId 사용자 ID
     * @param requestDTO 목표 생성 요청 정보
     * @return 생성된 목표 정보 (ID, 추천 상품 리스트 등)
     */
    public GoalCreateResponseDTO createGoal(UUID userId, GoalCreateRequestDTO requestDTO);

    /**
     * 목표 보고서로 저장
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     * @param selectedProductId 선택한 상품 ID
     * @return 수정된 목표 요약 정보
     */
    public GoalSummaryResponseDTO publishAsReport(UUID userId, Long goalId, Long selectedProductId);

    /**
     * 목표 달성 완료 처리
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     * @return 수정된 목표 요약 정보
     */
    public GoalSummaryResponseDTO achieveGoal(UUID userId, Long goalId);
}
