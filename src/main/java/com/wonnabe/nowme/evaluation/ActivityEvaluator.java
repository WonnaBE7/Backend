package com.wonnabe.nowme.evaluation;

import com.wonnabe.nowme.dto.NowMeRequestDTO;
import com.wonnabe.nowme.mapper.NowMeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 금융활동성 평가기 (Extrovert ↔ Introvert)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityEvaluator {

    private final NowMeMapper nowMeMapper;

    // 금융활동성 관련 설문 문항 인덱스 (12문항 중 4,5,6번)
    private static final int[] ACTIVITY_QUESTION_INDICES = {3, 4, 5}; // 4,5,6번 문항

    /**
     * 금융활동성 정량 점수 계산
     */
    public double calculateQuantScore(String userId) {
        try {
            log.debug("금융활동성 정량 계산 시작 - userId: {}", userId);

            int householdSize = Math.max(nowMeMapper.getHouseholdSize(userId), 1);
            double sqrtHousehold = Math.sqrt(householdSize);

            // 1. 계좌 다양성 (최대 4종류)
            int categoryCount = nowMeMapper.getAccountCategoryCount(userId);
            double accountScore = bound(categoryCount / 4.0);

            // 2. 금융상품 가입 수 (저축 + 보험, cap 10개)
            int savingsCount = nowMeMapper.getSavingsProductCount(userId);
            int insuranceCount = nowMeMapper.getInsuranceProductCount(userId);
            int totalProducts = savingsCount + insuranceCount;
            double productScore = bound(Math.min(totalProducts, 10) / 10.0);

            // 3. 월평균 거래 수 (가구원 수 보정)
            int transactionCount = nowMeMapper.getMonthlyTransactionCount(userId);
            double adjustedTxn = transactionCount / sqrtHousehold;
            double txnScore = bound(adjustedTxn / 43.2);

            // 4. 소비처 다양성 (가구원 수 보정)
            int merchantCategoryCount = nowMeMapper.getMonthlyMerchantCategoryCount(userId);
            double adjustedMerchant = merchantCategoryCount / sqrtHousehold;
            double merchantScore = bound(adjustedMerchant / 18.0);

            // 평균 계산
            double finalScore = average(accountScore, productScore, txnScore, merchantScore);

            log.debug("[정량 점수] userId: {}, 계좌: {}, 상품: {}, 거래: {}, 소비처: {}, 최종: {}",
                    userId, round(accountScore), round(productScore), round(txnScore),
                    round(merchantScore), round(finalScore));

            return round(finalScore);

        } catch (Exception e) {
            log.error("금융활동성 정량 계산 실패 - userId: {}", userId, e);
            return 0.5;  // 기본값
        }
    }

    /**
     * 금융활동성 정성 점수 계산 (0~1)
     * Q1: 금융앱 사용빈도, Q2: 금융정보 관심도, Q3: 금융상품 선택방식
     */
    public static double calculateQualScore(NowMeRequestDTO requestDTO) {
        List<Integer> answers = requestDTO.getAnswers();

        if (answers == null || answers.size() < 6) {
            log.warn("설문 답변 부족 - 기본값 0.5 반환");
            return 0.5;
        }

        // 금융활동성 관련 문항들 (4,5,6번 문항)
        double q1 = mapToActivityScore(answers.get(ACTIVITY_QUESTION_INDICES[0]), 1); // 금융앱 사용빈도
        double q2 = mapToActivityScore(answers.get(ACTIVITY_QUESTION_INDICES[1]), 2); // 금융정보 관심도
        double q3 = mapToActivityScore(answers.get(ACTIVITY_QUESTION_INDICES[2]), 3); // 상품선택 방식

        double finalScore = roundTo3DecimalPlaces((q1 + q2 + q3) / 3);

        log.debug("[정성 점수] Q1: {}, Q2: {}, Q3: {}, 최종: {}", q1, q2, q3, finalScore);

        return finalScore;
    }

    /**
     * 금융활동성 최종 점수 계산 (정량 60% + 정성 40%)
     */
    public double calculateFinalScore(String userId, NowMeRequestDTO requestDTO) {
        try {
            double quantScore = calculateQuantScore(userId);
            double qualScore = calculateQualScore(requestDTO);

            System.out.println("[Activity] 정량: " + quantScore + ", 정성: " + qualScore);

            double finalScore = (quantScore * 0.6) + (qualScore * 0.4);

            log.info("금융활동성 최종 점수 - userId: {}, 정량: {}, 정성: {}, 최종: {}",
                    userId, roundTo3DecimalPlaces(quantScore), roundTo3DecimalPlaces(qualScore),
                    roundTo3DecimalPlaces(finalScore));

            return roundTo3DecimalPlaces(finalScore);

        } catch (Exception e) {
            log.error("금융활동성 최종 점수 계산 실패 - userId: {}", userId, e);
            return 0.5;
        }
    }

    /**
     * 금융활동성 전용 점수 매핑 (문항별로 다른 매핑)
     */
    private static double mapToActivityScore(int answer, int questionType) {
        return switch (questionType) {
            case 1 -> // Q1: 금융앱 사용빈도 (4단계)
                    switch (answer) {
                        case 1 -> 1.0;  // 거의 매일
                        case 2 -> 0.8;  // 주 2-3회
                        case 3 -> 0.5;  // 월 2-3회
                        case 4 -> 0.0;  // 거의 안 함
                        default -> 0.5;
                    };
            case 2 -> // Q2: 금융정보 관심도 (3단계)
                    switch (answer) {
                        case 1 -> 1.0;  // 자주
                        case 2 -> 0.5;  // 가끔
                        case 3 -> 0.0;  // 거의 관심 없음
                        default -> 0.5;
                    };
            case 3 -> // Q3: 금융상품 선택방식 (4단계)
                    switch (answer) {
                        case 1 -> 1.0;  // 여러 곳 비교해서 직접 선택
                        case 2 -> 0.6;  // 추천받아 간단히 비교
                        case 3 -> 0.2;  // 추천 그대로 선택
                        case 4 -> 0.0;  // 기존 거래처에서 가입
                        default -> 0.5;
                    };
            default -> 0.5;
        };
    }

    // 평균 계산
    private double average(double... values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    // 소수점 3자리 반올림
    private static double roundTo3DecimalPlaces(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    // 0.0 ~ 1.0 사이로 제한하는 bound 함수
    private double bound(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    // 소수점 3자리 반올림 (간단 버전)
    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}