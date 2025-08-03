package com.wonnabe.nowme.evaluation;

import com.wonnabe.nowme.domain.UserVector;
import com.wonnabe.nowme.dto.NowMeRequestDTO;

public class ActivityEvaluator {

    // 정량 평가: 금융상품 개수 + 거래 횟수
    public static double calculateQuantScore(UserVector userVector) {
        // TODO: 정량 계산 로직 구현
        return 0.0;
    }

    // 정성 평가: 금융활동 관심도 + 자산관리 의지
    public static double calculateQualScore(NowMeRequestDTO requestDTO) {
        // TODO: 정성 계산 로직 구현
        return 0.0;
    }
}