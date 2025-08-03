package com.wonnabe.nowme.evaluation;

import com.wonnabe.nowme.dto.NowMeRequestDTO;
import com.wonnabe.nowme.mapper.NowMeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 계획방식 평가기 (Thinking ↔ Feeling)
 * - 목표 관리, 저축 계획, 소비 패턴 기반의 정량 점수
 * - 계획성 관련 설문 기반의 정성 점수
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanningEvaluator {

    private final NowMeMapper nowMeMapper;

    // 계획방식 관련 설문 문항 인덱스 (12문항 중 7,8,9번)
    private static final int[] PLANNING_QUESTION_INDICES = {6, 7, 8}; // 7,8,9번 문항

    /**
     * 🔹 계획방식 정량 점수 계산 (0~1)
     * - 목표 관리 능력, 저축 계획 준수율, 소비 패턴 안정성 → 3개 항목 평균
     */
    public double calculateQuantScore(Long userId) {
        try {
            log.debug("🔍 계획방식 정량 계산 시작 - userId: {}", userId);

            // 1. 목표 관리 능력 (Goal Management Score)
            double goalManagementScore = calculateGoalManagementScore(userId);

            // 2. 저축 계획 준수율 (Savings Discipline Score)
            double savingsDisciplineScore = calculateSavingsDisciplineScore(userId);

            // 3. 소비 패턴 안정성 (Spending Stability Score)
            double spendingStabilityScore = calculateSpendingStabilityScore(userId);

            // 3개 항목 평균 계산
            double finalScore = average(goalManagementScore, savingsDisciplineScore, spendingStabilityScore);

            log.debug("🔍 [정량 점수] userId: {}, 목표관리: {}, 저축준수: {}, 소비안정: {}, 최종: {}",
                    userId, round(goalManagementScore), round(savingsDisciplineScore),
                    round(spendingStabilityScore), round(finalScore));

            return round(finalScore);

        } catch (Exception e) {
            log.error("❗ 계획방식 정량 계산 실패 - userId: {}", userId, e);
            return 0.5; // 기본값
        }
    }

    /**
     * 🔹 계획방식 정성 점수 계산 (0~1)
     * - Q1: 예산 관리 철학, Q2: 예상치 못한 지출 반응, Q3: 월급 사용 우선순위
     */
    public static double calculateQualScore(NowMeRequestDTO requestDTO) {
        List<Integer> answers = requestDTO.getAnswers();

        if (answers == null || answers.size() < 9) {
//            log.warn("❗ 설문 답변 부족 - 기본값 0.5 반환");
            return 0.5;
        }

        // 계획방식 관련 문항들 (7,8,9번 문항)
        double q1 = mapToPlanningScore(answers.get(PLANNING_QUESTION_INDICES[0]), 1); // 예산 관리 철학
        double q2 = mapToPlanningScore(answers.get(PLANNING_QUESTION_INDICES[1]), 2); // 예상치 못한 지출 반응
        double q3 = mapToPlanningScore(answers.get(PLANNING_QUESTION_INDICES[2]), 3); // 월급 사용 우선순위

        double finalScore = roundTo3DecimalPlaces((q1 + q2 + q3) / 3);

        log.debug("🔍 [정성 점수] Q1: {}, Q2: {}, Q3: {}, 최종: {}", q1, q2, q3, finalScore);

        return finalScore;
    }

    /**
     * 🔹 계획방식 최종 점수 계산 (정량 60% + 정성 40%)
     */
    public double calculateFinalScore(Long userId, NowMeRequestDTO requestDTO) {
        try {
            double quantScore = calculateQuantScore(userId);
            double qualScore = calculateQualScore(requestDTO);

            double finalScore = (quantScore * 0.6) + (qualScore * 0.4);

            log.info("✅ 계획방식 최종 점수 - userId: {}, 정량: {}, 정성: {}, 최종: {}",
                    userId, roundTo3DecimalPlaces(quantScore), roundTo3DecimalPlaces(qualScore),
                    roundTo3DecimalPlaces(finalScore));

            return roundTo3DecimalPlaces(finalScore);

        } catch (Exception e) {
            log.error("❗ 계획방식 최종 점수 계산 실패 - userId: {}", userId, e);
            return 0.5;
        }
    }

    /**
     * 🔸 목표 관리 능력 계산
     * - 목표 설정 여부 + 목표 진척률 평균
     */
    private double calculateGoalManagementScore(Long userId) {
        try {
            // 목표 개수 확인
            int goalCount = nowMeMapper.getGoalCount(userId);
            double goalExistsScore = (goalCount > 0) ? 0.5 : 0.0;

            // 목표 진척률 평균 (progress_rate는 0~100 값으로 가정)
            double avgProgressRate = nowMeMapper.getAverageGoalProgressRate(userId);
            double progressScore = (goalCount > 0) ? bound(avgProgressRate / 100.0) : 0.5; // 목표 없으면 중립값

            // 최종 목표 관리 점수
            double goalScore = (goalExistsScore + progressScore) / 2;

            log.debug("🔍 목표관리 - 목표개수: {}, 설정점수: {}, 진척률: {}%, 진척점수: {}, 최종: {}",
                    goalCount, goalExistsScore, avgProgressRate, progressScore, goalScore);

            return goalScore;

        } catch (Exception e) {
            log.warn("❗ 목표 관리 점수 계산 실패 - userId: {}, 기본값 0.5 반환", userId, e);
            return 0.5;
        }
    }

    /**
     * 🔸 저축 계획 준수율 계산
     * - user_goal의 monthly_save_amount vs User_Savings의 monthly_payment 비교
     */
    private double calculateSavingsDisciplineScore(Long userId) {
        try {
            // 목표한 월 저축 금액 합계
            double plannedMonthlySaving = nowMeMapper.getPlannedMonthlySaving(userId);

            // 실제 월 저축 금액 합계
            double actualMonthlySaving = nowMeMapper.getActualMonthlySaving(userId);

            if (plannedMonthlySaving == 0) {
                // 저축 목표가 없으면 실제 저축 여부로만 판단
                return (actualMonthlySaving > 0) ? 0.7 : 0.3;
            }

            // 계획 대비 실제 저축 비율
            double savingsRatio = actualMonthlySaving / plannedMonthlySaving;
            double disciplineScore = bound(savingsRatio); // 1.0을 초과해도 1.0으로 제한

            log.debug("🔍 저축준수 - 계획: {}, 실제: {}, 비율: {}, 점수: {}",
                    plannedMonthlySaving, actualMonthlySaving, savingsRatio, disciplineScore);

            return disciplineScore;

        } catch (Exception e) {
            log.warn("❗ 저축 계획 준수율 계산 실패 - userId: {}, 기본값 0.5 반환", userId, e);
            return 0.5;
        }
    }

    /**
     * 🔸 소비 패턴 안정성 계산
     * - Summaries_Cache의 monthly 데이터로 변동계수(CV) 계산
     */
    private double calculateSpendingStabilityScore(Long userId) {
        try {
            // 최근 6개월 월별 소비 금액 표준편차
            double monthlySpendingStdDev = nowMeMapper.getMonthlySpendingStdDev(userId);

            // 최근 6개월 월별 소비 금액 평균
            double monthlySpendingAvg = nowMeMapper.getMonthlySpendingAverage(userId);

            if (monthlySpendingAvg == 0) {
                log.debug("🔍 소비안정성 - 소비 데이터 없음, 기본값 0.5 반환");
                return 0.5;
            }

            // 변동계수(CV) = 표준편차 / 평균
            double coefficientOfVariation = monthlySpendingStdDev / monthlySpendingAvg;

            // CV가 낮을수록 안정적 → 높은 점수
            // CV 0.3을 기준으로 정규화 (일반적으로 CV 0.3 이하면 안정적)
            double stabilityScore = bound(Math.max(0.3 - coefficientOfVariation, 0.0) / 0.3);

            log.debug("🔍 소비안정성 - 평균: {}, 표준편차: {}, CV: {}, 점수: {}",
                    monthlySpendingAvg, monthlySpendingStdDev, coefficientOfVariation, stabilityScore);

            return stabilityScore;

        } catch (Exception e) {
            log.warn("❗ 소비 패턴 안정성 계산 실패 - userId: {}, 기본값 0.5 반환", userId, e);
            return 0.5;
        }
    }

    /**
     * 🔸 계획방식 전용 점수 매핑
     */
    private static double mapToPlanningScore(int answer, int questionType) {
        return switch (questionType) {
            case 1 -> // Q1: 예산 관리 철학 (3단계)
                    switch (answer) {
                        case 1 -> 0.0;  // 계획 없는 게 더 자유롭다
                        case 2 -> 0.5;  // 대략은 세우지만 자주 바뀐다
                        case 3 -> 1.0;  // 구체적 계획 없이는 불안하다
                        default -> 0.5;
                    };
            case 2 -> // Q2: 예상치 못한 지출 반응 (3단계)
                    switch (answer) {
                        case 1 -> 0.0;  // 큰 고민 없이 쓴다
                        case 2 -> 0.5;  // 상황에 따라 고민한다
                        case 3 -> 1.0;  // 먼저 예산 조정부터 한다
                        default -> 0.5;
                    };
            case 3 -> // Q3: 월급 사용 우선순위 (3단계)
                    switch (answer) {
                        case 1 -> 0.0;  // 평소 갖고 싶던 걸 산다
                        case 2 -> 0.5;  // 필요한 것 먼저 사고 일부는 저축
                        case 3 -> 1.0;  // 저축/지출 계획부터 세운다
                        default -> 0.5;
                    };
            default -> 0.5;
        };
    }

    // 🔸 평균 계산
    private double average(double... values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    // 🔸 소수점 3자리 반올림
    private static double roundTo3DecimalPlaces(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    // 🔸 0.0 ~ 1.0 사이로 제한하는 bound 함수
    private double bound(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    // 🔸 소수점 3자리 반올림 (간단 버전)
    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}