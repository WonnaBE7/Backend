package com.wonnabe.nowme.evaluation;

import com.wonnabe.nowme.domain.UserVector;
import com.wonnabe.nowme.dto.NowMeRequestDTO;

public class PlanningEvaluator {

    /**
     * 🔹 정량 점수 계산: 목표 설정 여부, 달성률 등
     */
    public static double calculateQuantScore(UserVector userVector) {
        // TODO: 금융 계획 관련 정량 점수 계산
        return 0.0;
    }

    /**
     * 🔹 정성 점수 계산: 설문 응답 기반 계획 성향
     */
    public static double calculateQualScore(NowMeRequestDTO requestDTO) {
        // TODO: 금융 계획 관련 정성 점수 계산
        return 0.0;
    }
}