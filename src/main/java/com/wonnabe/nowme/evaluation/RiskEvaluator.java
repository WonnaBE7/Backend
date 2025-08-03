package com.wonnabe.nowme.evaluation;

import com.wonnabe.nowme.domain.UserVector;
import com.wonnabe.nowme.dto.NowMeRequestDTO;

public class RiskEvaluator {

    /**
     * 🔹 정량 점수 계산: 투자 비중, 포트폴리오 분산 등
     */
    public static double calculateQuantScore(UserVector userVector) {
        // TODO: 투자 위험 성향 관련 정량 점수 계산
        return 0.0;
    }

    /**
     * 🔹 정성 점수 계산: 위험 수용도, 설문 응답 등
     */
    public static double calculateQualScore(NowMeRequestDTO requestDTO) {
        // TODO: 투자 위험 관련 정성 점수 계산
        return 0.0;
    }
}