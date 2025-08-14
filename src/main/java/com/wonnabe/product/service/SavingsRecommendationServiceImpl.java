package com.wonnabe.product.service;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserIncomeInfoVO;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO.PersonaRecommendation;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO.RecommendedSavings;
import com.wonnabe.product.mapper.SavingsRecommendationMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service("SavingsRecommendationServiceImpl")
@RequiredArgsConstructor
public class SavingsRecommendationServiceImpl implements SavingsRecommendationService {

    private final SavingsRecommendationMapper recommendationMapper;

    // 페르소나 이름 매핑
    private static final Map<Integer, String> PERSONA_NAMES = Map.ofEntries(
        Map.entry(1, "자린고비형"),
        Map.entry(2, "소확행형"),
        Map.entry(3, "YOLO형"),
        Map.entry(4, "경험 소중형"),
        Map.entry(5, "새싹 투자형"),
        Map.entry(6, "공격 투자형"),
        Map.entry(7, "미래 준비형"),
        Map.entry(8, "가족 중심형"),
        Map.entry(9, "루틴러형"),
        Map.entry(10, "현상 유지형"),
        Map.entry(11, "균형 성장형"),
        Map.entry(12, "대문자P형")
    );

    // 페르소나별 정확한 가중치 [금리, 복리, 우대조건, 중도해지, 가입한도]
    private static final Map<Integer, double[]> PERSONA_WEIGHTS = new HashMap<>() {{
        put(1, new double[]{0.3, 0.1, 0.2, 0.3, 0.2});    // 자린고비형
        put(2, new double[]{0.3, 0.15, 0.2, 0.2, 0.15});  // 소확행형
        put(3, new double[]{0.35, 0.1, 0.15, 0.3, 0.1});  // YOLO형
        put(4, new double[]{0.25, 0.15, 0.25, 0.2, 0.15}); // 경험 소중형
        put(5, new double[]{0.3, 0.2, 0.2, 0.2, 0.1});    // 새싹 투자형
        put(6, new double[]{0.4, 0.25, 0.15, 0.1, 0.1});  // 공격 투자형
        put(7, new double[]{0.25, 0.25, 0.3, 0.1, 0.1});  // 미래 준비형
        put(8, new double[]{0.2, 0.2, 0.2, 0.3, 0.1});    // 가족 중심형
        put(9, new double[]{0.25, 0.2, 0.2, 0.2, 0.15});  // 루틴러형
        put(10, new double[]{0.2, 0.15, 0.2, 0.3, 0.15}); // 현상 유지형
        put(11, new double[]{0.3, 0.2, 0.2, 0.2, 0.1});   // 균형 성장형
        put(12, new double[]{0.35, 0.1, 0.1, 0.35, 0.1}); // 대문자P형
    }};

    @Override
    public SavingsRecommendationResponseDTO recommendSavings(String userId, int topN) {
        // 1. 사용자 정보 조회
        UserIncomeInfoVO userInfo = recommendationMapper.getUserIncomeInfo(userId);
        log.info("### [DEBUG] Fetched userInfo for savings recommend: {}", userInfo);

        if (userInfo == null || userInfo.getPersonaIds() == null || userInfo.getPersonaIds().isEmpty()) {
            log.warn("사용자 정보 또는 페르소나 ID가 없어 적금 추천을 진행할 수 없습니다. userId: {}", userId);
            return new SavingsRecommendationResponseDTO(userId, new ArrayList<>());
        }

        // 2. 모든 적금 상품과 점수 조회
        List<SavingsProductVO> productsWithScores = recommendationMapper.getAllSavingsScores();
        if (productsWithScores == null || productsWithScores.isEmpty()) {
            log.warn("추천할 적금 상품이 없어 적금 추천을 진행할 수 없습니다.");
            return new SavingsRecommendationResponseDTO(userId, new ArrayList<>());
        }

        // 3. 결과 객체 생성
        SavingsRecommendationResponseDTO response = new SavingsRecommendationResponseDTO();
        response.setUserId(userId);
        response.setRecommendationsByPersona(new ArrayList<>());

        // 4. 각 페르소나별로 추천
        for (Integer personaId : userInfo.getPersonaIds()) {
            // 기본 가중치 가져오기
            double[] baseWeights = PERSONA_WEIGHTS.get(personaId).clone();

            // 소득/고용상태로 가중치 조정
            double[] adjustedWeights = adjustWeightsByIncome(
                    baseWeights,
                    userInfo.getIncomeSourceType(),
                    userInfo.getIncomeEmploymentStatus()
            );

            // 각 상품의 점수 계산
            List<ProductWithScore> scoredProducts = new ArrayList<>();
            for (SavingsProductVO product : productsWithScores) {
                double score = calculateScore(product, adjustedWeights);
                scoredProducts.add(new ProductWithScore(product, score));
            }

            // 점수 순으로 정렬
            scoredProducts.sort((a, b) -> Double.compare(b.score, a.score));

            // 상위 N개 선택
            PersonaRecommendation personaRec = new PersonaRecommendation();
            personaRec.setPersonaId(personaId);
            personaRec.setPersonaName(PERSONA_NAMES.get(personaId));
            personaRec.setProducts(new ArrayList<>());

            for (int i = 0; i < Math.min(topN, scoredProducts.size()); i++) {
                ProductWithScore item = scoredProducts.get(i);
                SavingsProductVO product = item.product;

                RecommendedSavings rec = new RecommendedSavings();
                rec.setProductId(String.valueOf(product.getProductId()));
                rec.setProductName(product.getProductName());
                rec.setBankName(product.getBankName());
                rec.setBaseRate(product.getBaseRate() != null ? product.getBaseRate() : 0.0f);
                rec.setMaxRate(product.getMaxRate() != null ? product.getMaxRate() : 0.0f);
                rec.setScore(item.score);
                rec.setProductType("savings");

                personaRec.getProducts().add(rec);
            }

            response.getRecommendationsByPersona().add(personaRec);
        }

        return response;
    }

    @Override
    public Map<Integer, double[]> getPersonaWeights() {
        return PERSONA_WEIGHTS;
    }

    // 소득/고용상태에 따른 가중치 조정
    private double[] adjustWeightsByIncome(double[] weights, String incomeSource, String employment) {
        double[] adjusted = weights.clone();

        // (‼️ 수정)
        // 소득원별 조정
        if (incomeSource != null && !incomeSource.isEmpty()) {
            switch (incomeSource) {
                case "근로소득":
                    adjusted[0] += 0.05;  // 금리
                    adjusted[1] += 0.03;  // 단복리
                    break;
                case "사업소득":
                    adjusted[3] += 0.05;  // 중도해지
                    adjusted[2] += 0.03;  // 우대조건
                    break;
                case "기타소득":
                    adjusted[4] += 0.05;  // 최대한도
                    adjusted[3] += 0.03;  // 중도해지
                    break;
            }
        }

        // 고용상태별 조정
        if (employment != null && !employment.isEmpty()) {
            switch (employment) {
                case "정규직":
                    adjusted[0] += 0.05;  // 금리
                    adjusted[1] += 0.03;  // 단복리
                    break;
                case "학생":
                    adjusted[2] += 0.05;  // 우대조건
                    adjusted[4] += 0.03;  // 최대한도
                    break;
                case "무직":
                    adjusted[3] += 0.05;  // 중도해지
                    adjusted[1] += 0.03;  // 단복리
                    break;
            }
        }


        // 정규화 (합 = 1)
        return normalizeWeights(adjusted);
    }

    // 점수 계산
    public double calculateScore(SavingsProductVO score, double[] weights) {
        return (weights[0] * score.getScoreInterestRate() +
                weights[1] * score.getScoreInterestType() +
                weights[2] * score.getScorePreferentialCondition() +
                weights[3] * score.getScoreCancelBenefit() +
                weights[4] * score.getScoreMaxAmount()) ; // 100점 만점
    }

    // 가중치 정규화
    public double[] normalizeWeights(double[] weights) {
        double sum = Arrays.stream(weights).sum();
        if (sum == 0) {
            return weights;
        }
        return Arrays.stream(weights).map(w -> w / sum).toArray();
    }

    // 내부 클래스: 상품과 점수
    private static class ProductWithScore {
        SavingsProductVO product;
        double score;

        ProductWithScore(SavingsProductVO product, double score) {
            this.product = product;
            this.score = score;
        }
    }
}
