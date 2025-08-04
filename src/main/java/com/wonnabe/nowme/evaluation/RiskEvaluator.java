package com.wonnabe.nowme.evaluation;

import com.wonnabe.nowme.dto.NowMeRequestDTO;
import com.wonnabe.nowme.mapper.NowMeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 리스크성향 평가기 (Risk Averse ↔ Risk Taking)
 * - 투자 계좌 및 저축상품 기반의 정량 점수
 * - 리스크 인식 관련 설문 기반의 정성 점수
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiskEvaluator {

    private final NowMeMapper nowMeMapper;

    // 리스크성향 관련 설문 문항 인덱스 (12문항 중 10,11,12번)
    private static final int[] RISK_QUESTION_INDICES = {9, 10, 11}; // 10,11,12번 문항

    // 🔸 투자(위험자산) 계좌 카테고리
    private static final Set<String> INVESTMENT_CATEGORIES = Set.of("투자");

    // 🔸 기준값들 (기획안 기준)
    private static final double RISK_ASSET_RATIO_BASELINE = 0.6;    // 60%
    private static final double INVESTMENT_PRODUCT_BASELINE = 3.0;  // 3개
    private static final double EXPECTED_RETURN_BASELINE = 5.0;     // 5.0%

    /**
     * 🔹 리스크성향 정량 점수 계산 (0~1)
     * - 위험자산 비율, 투자상품 다양성, 기대수익률 평균 → 3개 항목 평균
     */
    public double calculateQuantScore(String userId) {
        try {
            log.debug("🔍 리스크성향 정량 계산 시작 - userId: {}", userId);

            // 1. 위험자산 비율 = 투자 계좌 잔액 / 전체 계좌 잔액
            double totalBalance = nowMeMapper.getTotalBalance(userId);
            double investmentBalance = nowMeMapper.getBalanceByCategories(userId, INVESTMENT_CATEGORIES);
            double riskAssetRatio = (totalBalance == 0) ? 0.0 : (investmentBalance / totalBalance);
            double riskAssetScore = bound(riskAssetRatio / RISK_ASSET_RATIO_BASELINE);

            // 2. 투자상품 다양성 = 투자 계좌 개수 (계좌명 기준)
            int investmentAccountCount = nowMeMapper.getAccountCountByCategories(userId, INVESTMENT_CATEGORIES);
            double productDiversityScore = bound(investmentAccountCount / INVESTMENT_PRODUCT_BASELINE);

            // 3. 기대수익률 평균 = 사용자가 가입한 저축상품 금리 평균값
            double avgExpectedRate = nowMeMapper.getAvgSavingsRate(userId);
            double expectedReturnScore = bound(avgExpectedRate / EXPECTED_RETURN_BASELINE);

            // 🔸 예외 처리: 투자 계좌가 전혀 없을 경우 완전한 안정형으로 간주
            if (investmentAccountCount == 0 && investmentBalance == 0) {
                log.debug("📝 투자 계좌 없음 - 안정형으로 판단 (0.0)");
                return 0.0;
            }

            // 3개 항목 평균 계산
            double finalScore = average(riskAssetScore, productDiversityScore, expectedReturnScore);

            log.debug("🔍 [정량 점수] userId: {}, 위험자산비율: {}, 상품다양성: {}, 기대수익률: {}, 최종: {}",
                    userId, round(riskAssetScore), round(productDiversityScore),
                    round(expectedReturnScore), round(finalScore));

            return round(finalScore);

        } catch (Exception e) {
            log.error("❗ 리스크성향 정량 계산 실패 - userId: {}", userId, e);
            return 0.5; // 기본값
        }
    }

    /**
     * 🔹 리스크성향 정성 점수 계산 (0~1)
     * - Q1: 고위험 고수익 투자 선호, Q2: 손실 대응 방식, Q3: 리스크 인식
     */
    public static double calculateQualScore(NowMeRequestDTO requestDTO) {
        List<Integer> answers = requestDTO.getAnswers();

        if (answers == null || answers.size() < 12) {
            log.warn("❗ 설문 답변 부족 - 기본값 0.5 반환");
            return 0.5;
        }

        // 리스크성향 관련 문항들 (10,11,12번 문항)
        double q1 = mapToRiskScore(answers.get(RISK_QUESTION_INDICES[0]), 1); // 투자 선호
        double q2 = mapToRiskScore(answers.get(RISK_QUESTION_INDICES[1]), 2); // 손실 대응
        double q3 = mapToRiskScore(answers.get(RISK_QUESTION_INDICES[2]), 3); // 리스크 인식

        double finalScore = roundTo3DecimalPlaces((q1 + q2 + q3) / 3);

        log.debug("🔍 [정성 점수] Q1: {}, Q2: {}, Q3: {}, 최종: {}", q1, q2, q3, finalScore);

        return finalScore;
    }

    /**
     * 🔹 리스크성향 최종 점수 계산 (정량 60% + 정성 40%)
     */
    public double calculateFinalScore(String userId, NowMeRequestDTO requestDTO) {
        try {
            double quantScore = calculateQuantScore(userId);
            double qualScore = calculateQualScore(requestDTO);

            System.out.println("🔍 [Activity] 정량: " + quantScore + ", 정성: " + qualScore);

            double finalScore = (quantScore * 0.6) + (qualScore * 0.4);

            log.info("✅ 리스크성향 최종 점수 - userId: {}, 정량: {}, 정성: {}, 최종: {}",
                    userId, roundTo3DecimalPlaces(quantScore), roundTo3DecimalPlaces(qualScore),
                    roundTo3DecimalPlaces(finalScore));

            return roundTo3DecimalPlaces(finalScore);

        } catch (Exception e) {
            log.error("❗ 리스크성향 최종 점수 계산 실패 - userId: {}", userId, e);
            return 0.5;
        }
    }

    /**
     * 🔸 리스크성향 전용 점수 매핑 (기획안 기준)
     */
    private static double mapToRiskScore(int answer, int questionType) {
        return switch (questionType) {
            case 1 -> // Q1: 고위험 고수익 투자 선호 (3단계)
                    switch (answer) {
                        case 1 -> 0.0;  // 전혀 그렇지 않다
                        case 2 -> 0.5;  // 보통이다
                        case 3 -> 1.0;  // 매우 그렇다
                        default -> 0.5;
                    };
            case 2 -> // Q2: 손실 대응 방식 (3단계)
                    switch (answer) {
                        case 1 -> 0.0;  // 즉시 중단하고 안전자산으로 전환
                        case 2 -> 0.5;  // 일부 조정
                        case 3 -> 1.0;  // 일정 기간 유지 후 판단
                        default -> 0.5;
                    };
            case 3 -> // Q3: 리스크 인식 (3단계)
                    switch (answer) {
                        case 1 -> 0.0;  // 피해야 할 것
                        case 2 -> 0.5;  // 관리 가능한 요소
                        case 3 -> 1.0;  // 기회라고 생각함
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