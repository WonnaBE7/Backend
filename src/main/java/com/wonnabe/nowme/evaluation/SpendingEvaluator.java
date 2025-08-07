package com.wonnabe.nowme.evaluation;

import com.wonnabe.nowme.dto.NowMeRequestDTO;
import com.wonnabe.nowme.mapper.NowMeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 소비패턴 평가기 (Sensing ↔ Intuition)
 * - 카드 소비 내역 기반의 정량 점수
 * - 설문 응답 기반의 정성 점수
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpendingEvaluator {

    private final NowMeMapper nowMeMapper;

    // 필수 소비 카테고리
    private static final Set<String> ESSENTIAL_CATEGORIES = Set.of("food", "transport", "living", "fixcost");

    // 상수값 외부화
    private static final double DIVERSITY_BASELINE = 4.0; // 8.2
    private static final double SPENDING_RATE_BASELINE = 0.4; // 0.673
    private static final double CONSISTENCY_BASELINE = 35000; // 67000.0

    /**
     * 소비 성향 정량 점수 계산 (0~1)
     * - 소비 기록 기반 4개 항목 점수 → 평균
     */
    public double calculateQuantScore(String userId) {
        try {
            log.debug("소비패턴 정량 계산 시작 - userId: {}", userId);

            double totalSpending = nowMeMapper.getTotalSpending(userId);
            double essentialSpending = nowMeMapper.getSpendingByCategories(userId, ESSENTIAL_CATEGORIES);
            double essentialScore = (totalSpending == 0) ? 0.5 : (1.0 - (essentialSpending / totalSpending));

            int householdSize = nowMeMapper.getHouseholdSize(userId);
            int categoryCount = nowMeMapper.getMonthlySelectableCategoryCount(userId);
            double diversityScore = categoryCount / Math.sqrt(Math.max(householdSize, 1));
            diversityScore = Math.min(diversityScore / DIVERSITY_BASELINE, 1.0);

            double annualIncome = nowMeMapper.getAnnualIncome(userId);
            double monthlyIncome = (annualIncome / 12.0);
            double spendingRate = (monthlyIncome == 0) ? 1.0 : (totalSpending / monthlyIncome);
            double spendingRateScore = Math.min(spendingRate / SPENDING_RATE_BASELINE, 1.0);

            double weeklyStdDev = nowMeMapper.getWeeklySpendingStdDev(userId);
            double consistencyScore = Math.min(Math.max((CONSISTENCY_BASELINE - weeklyStdDev) / CONSISTENCY_BASELINE, 0), 1.0);

            double finalScore = average(essentialScore, diversityScore, spendingRateScore, consistencyScore);

            // 상세 로깅
            log.debug("[정량 점수] userId: {}, 필수소비: {}, 다양성: {}, 소비율: {}, 일관성: {}, 최종: {}",
                    userId, roundTo3DecimalPlaces(essentialScore), roundTo3DecimalPlaces(diversityScore),
                    roundTo3DecimalPlaces(spendingRateScore), roundTo3DecimalPlaces(consistencyScore),
                    roundTo3DecimalPlaces(finalScore));

            return finalScore;

        } catch (Exception e) {
            log.error("소비패턴 정량 계산 실패 - userId: {}", userId, e);
            return 0.5; // 기본값 반환
        }
    }

    /**
     * 소비 성향 정성 점수 계산 (0~1)
     * - 설문 응답 기반 3문항 해석 → 평균
     */
    public static double calculateQualScore(NowMeRequestDTO requestDTO) {
        List<Integer> answers = requestDTO.getAnswers();

        if (answers == null || answers.size() < 3) {
            log.warn("설문 답변 부족 - 기본값 0.5 반환");
            return 0.5;
        }

        double q1 = mapToScore(answers.get(0)); // 소비 기준
        double q2 = mapToScore(answers.get(1)); // 충동 구매 빈도
        double q3 = mapToScore(answers.get(2)); // 여유 자금 소비 방향

        double finalScore = roundTo3DecimalPlaces((q1 + q2 + q3) / 3);

        // 상세 로깅
        log.debug("[정성 점수] Q1: {}, Q2: {}, Q3: {}, 최종: {}", q1, q2, q3, finalScore);

        return finalScore;
    }

    /**
     * 소비패턴 최종 점수 계산 (정량 60% + 정성 40%)
     */
    public double calculateFinalScore(String userId, NowMeRequestDTO requestDTO) {
        try {
            double quantScore = calculateQuantScore(userId);
            double qualScore = calculateQualScore(requestDTO);

            System.out.println("[Activity] 정량: " + quantScore + ", 정성: " + qualScore);

            double finalScore = (quantScore * 0.6) + (qualScore * 0.4);

            log.info("소비패턴 최종 점수 - userId: {}, 정량: {}, 정성: {}, 최종: {}",
                    userId, roundTo3DecimalPlaces(quantScore), roundTo3DecimalPlaces(qualScore),
                    roundTo3DecimalPlaces(finalScore));

            return roundTo3DecimalPlaces(finalScore);

        } catch (Exception e) {
            log.error("소비패턴 최종 점수 계산 실패 - userId: {}", userId, e);
            return 0.5;
        }
    }

    // 선택지 점수 매핑 (1=0.0, 2=0.5, 3=1.0)
    private static double mapToScore(int answer) {
        return switch (answer) {
            case 1 -> 0.0;
            case 2 -> 0.5;
            case 3 -> 1.0;
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
}