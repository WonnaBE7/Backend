package com.wonnabe.product.service;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserIncomeInfoVO;
import com.wonnabe.product.dto.InsuranceRecommendationResponseDTO;
import com.wonnabe.product.dto.InsuranceRecommendationResponseDTO.PersonaRecommendation;
import com.wonnabe.product.dto.InsuranceRecommendationResponseDTO.RecommendedInsurance;
import com.wonnabe.product.mapper.InsuranceRecommendationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service("InsuranceRecommendationServiceImpl")
@RequiredArgsConstructor
public class InsuranceRecommendationServiceImpl implements InsuranceRecommendationService {

    private final InsuranceRecommendationMapper recommendationMapper;

    // ----------------------------
    // Persona 매핑
    // ----------------------------
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

    // ----------------------------
    // 보험 Persona별 가중치
    // ----------------------------
    private static final Map<String, Map<String, Double>> PERSONA_WEIGHTS_INSURANCE = new HashMap<>() {{
        put("자린고비형", new HashMap<>() {{ put("가격_경쟁력", 0.45); put("보장한도", 0.15); put("보장범위", 0.15); put("자기부담금", 0.15); put("환급범위", 0.10); }});
        put("소확행형", new HashMap<>() {{ put("가격_경쟁력", 0.35); put("보장한도", 0.20); put("보장범위", 0.20); put("자기부담금", 0.10); put("환급범위", 0.15); }});
        put("YOLO형", new HashMap<>() {{ put("가격_경쟁력", 0.30); put("보장한도", 0.20); put("보장범위", 0.25); put("자기부담금", 0.15); put("환급범위", 0.10); }});
        put("경험 소중형", new HashMap<>() {{ put("가격_경쟁력", 0.25); put("보장한도", 0.25); put("보장범위", 0.25); put("자기부담금", 0.15); put("환급범위", 0.10); }});
        put("새싹 투자형", new HashMap<>() {{ put("가격_경쟁력", 0.30); put("보장한도", 0.20); put("보장범위", 0.20); put("자기부담금", 0.20); put("환급범위", 0.10); }});
        put("공격 투자형", new HashMap<>() {{ put("가격_경쟁력", 0.40); put("보장한도", 0.20); put("보장범위", 0.15); put("자기부담금", 0.15); put("환급범위", 0.10); }});
        put("미래 준비형", new HashMap<>() {{ put("가격_경쟁력", 0.20); put("보장한도", 0.30); put("보장범위", 0.30); put("자기부담금", 0.10); put("환급범위", 0.10); }});
        put("가족 중심형", new HashMap<>() {{ put("가격_경쟁력", 0.20); put("보장한도", 0.30); put("보장범위", 0.25); put("자기부담금", 0.15); put("환급범위", 0.10); }});
        put("루틴러형", new HashMap<>() {{ put("가격_경쟁력", 0.25); put("보장한도", 0.25); put("보장범위", 0.25); put("자기부담금", 0.15); put("환급범위", 0.10); }});
        put("현상 유지형", new HashMap<>() {{ put("가격_경쟁력", 0.30); put("보장한도", 0.25); put("보장범위", 0.20); put("자기부담금", 0.15); put("환급범위", 0.10); }});
        put("균형 성장형", new HashMap<>() {{ put("가격_경쟁력", 0.25); put("보장한도", 0.25); put("보장범위", 0.25); put("자기부담금", 0.15); put("환급범위", 0.10); }});
        put("대문자P형", new HashMap<>() {{ put("가격_경쟁력", 0.30); put("보장한도", 0.20); put("보장범위", 0.20); put("자기부담금", 0.20); put("환급범위", 0.10); }});
    }};

    @Override
    public InsuranceRecommendationResponseDTO recommendInsurance(String userId, int topN) {
        // 1. 사용자 정보 조회
        UserIncomeInfoVO userIncomeInfo = recommendationMapper.getUserHealthInfo(userId);
        log.info("### [DEBUG] Fetched userIncomeInfo for insurance recommend: {}", userIncomeInfo);

        if (userIncomeInfo == null || userIncomeInfo.getPersonaIds() == null || userIncomeInfo.getPersonaIds().isEmpty()) {
            log.warn("사용자 건강 정보 또는 페르소나 ID가 없어 보험 추천을 진행할 수 없습니다. userId: {}", userId);
            return new InsuranceRecommendationResponseDTO(userId, new ArrayList<>());
        }

        // 2. 모든 보험 상품과 점수 조회
        List<InsuranceProductVO> allInsuranceProducts = recommendationMapper.getAllInsuranceScores();
        if (allInsuranceProducts == null || allInsuranceProducts.isEmpty()) {
            log.warn("추천할 보험 상품이 없어 보험 추천을 진행할 수 없습니다.");
            return new InsuranceRecommendationResponseDTO(userId, new ArrayList<>());
        }

        // 3. 결과 객체 생성
        InsuranceRecommendationResponseDTO response = new InsuranceRecommendationResponseDTO();
        response.setUserId(userId);
        response.setRecommendationsByPersona(new ArrayList<>());

        // 4. 각 페르소나별로 추천
        for (Integer personaId : userIncomeInfo.getPersonaIds()) {
            String personaName = PERSONA_NAMES.get(personaId);
            if (personaName == null) {
                log.warn("알 수 없는 페르소나 ID: {}", personaId);
                continue;
            }

            // 기본 가중치 가져오기
            Map<String, Double> baseWeights = new HashMap<>(PERSONA_WEIGHTS_INSURANCE.get(personaName));

            // 건강/생활습관으로 가중치 조정
            Map<String, Double> adjustedWeights = adjustWeightsByHealthAndLifestyle(
                    baseWeights,
                    userIncomeInfo.getSmokingStatus(),
                    userIncomeInfo.getFamilyMedicalHistory(),
                    userIncomeInfo.getPastMedicalHistory(),
                    userIncomeInfo.getExerciseFrequency(),
                    userIncomeInfo.getDrinkingFrequency()
            );

            // 각 상품의 점수 계산
            List<ProductWithScore<InsuranceProductVO>> scoredProducts = new ArrayList<>();
            double[] weightsArray = convertWeightsMapToArray(adjustedWeights);
            for (InsuranceProductVO product : allInsuranceProducts) {
                double score = calculateScore(product, weightsArray);
                scoredProducts.add(new ProductWithScore<>(product, score));
            }

            // 점수 순으로 정렬
            scoredProducts.sort((a, b) -> Double.compare(b.score, a.score));

            // 상위 N개 선택
            PersonaRecommendation personaRec = new PersonaRecommendation();
            personaRec.setPersonaId(personaId);
            personaRec.setPersonaName(personaName);
            personaRec.setProducts(new ArrayList<>());

            for (int i = 0; i < Math.min(topN, scoredProducts.size()); i++) {
                ProductWithScore<InsuranceProductVO> item = scoredProducts.get(i);
                InsuranceProductVO product = item.product;

                RecommendedInsurance rec = new RecommendedInsurance();
                rec.setProductId(String.valueOf(product.getProductId()));
                rec.setProductName(product.getProductName());
                rec.setProviderName(product.getProviderName());
                rec.setProductType("insurance");

                rec.setCoverageLimit(product.getCoverageLimit());
                rec.setNote(product.getNote());
                rec.setMyMoney(product.getMyMoney());

                rec.setScore(item.score);

                personaRec.getProducts().add(rec);
            }

            response.getRecommendationsByPersona().add(personaRec);
        }

        return response;
    }

    // 건강/생활습관에 따른 가중치 조정
    public Map<String, Double> adjustWeightsByHealthAndLifestyle(
            Map<String, Double> weights,
            int smokingStatus,
            int familyMedicalHistory,
            int pastMedicalHistory,
            int exerciseFrequency,
            int drinkingFrequency) {

        Map<String, Double> adjusted = new HashMap<>(weights);

        // (‼️ 수정)
        // 흡연 여부
        if ("1".equalsIgnoreCase(String.valueOf(smokingStatus))) {
            adjusted.compute("보장범위", (k, v) -> v != null ? v + 0.05 : 0.05);        // 질병 리스크 확대
            adjusted.compute("자기부담금수준", (k, v) -> v != null ? v + 0.03 : 0.03);  // 보험사 리스크 반영
        } else {
            adjusted.compute("가격_경쟁력", (k, v) -> v != null ? v + 0.05 : 0.05);     // 비흡연자 우대
            adjusted.compute("환급범위", (k, v) -> v != null ? v + 0.03 : 0.03);        // 장기 계약 유도
        }


        // 가족 병력
        if ("1".equalsIgnoreCase(String.valueOf(familyMedicalHistory))) {
                adjusted.compute("보장한도", (k, v) -> v != null ? v + 0.05 : 0.05);    // 만성질환 대비
                adjusted.compute("자기부담금수준", (k, v) -> v != null ? v + 0.03 : 0.03);  // 보험사 부담 반영
            }



        // 과거 병력
        if ("1".equalsIgnoreCase(String.valueOf(pastMedicalHistory))) {
            adjusted.compute("보장범위", (k, v) -> v != null ? v + 0.05 : 0.05);        // 재발 가능성 고려
            adjusted.compute("자기부담금수준", (k, v) -> v != null ? v + 0.05 : 0.05);  // 보험사 리스크 반영
        } else {
            adjusted.compute("가격_경쟁력", (k, v) -> v != null ? v + 0.05 : 0.05);     // 무병력 우대
        }


        // 운동 빈도
        if ("1".equalsIgnoreCase(String.valueOf(exerciseFrequency))) {
            adjusted.compute("가격_경쟁력", (k, v) -> v != null ? v + 0.05 : 0.05);     // 건강 습관 우대
            adjusted.compute("환급범위", (k, v) -> v != null ? v + 0.03 : 0.03);        // 장기계약 유지 유도
        } else if ("0".equalsIgnoreCase(String.valueOf(exerciseFrequency))) {
            adjusted.compute("보장한도", (k, v) -> v != null ? v + 0.05 : 0.05);        // 건강 리스크 고려
            adjusted.compute("자기부담금수준", (k, v) -> v != null ? v + 0.03 : 0.03);  // 보험사 리스크 반영
        }


        // 음주 빈도
        if ("1".equalsIgnoreCase(String.valueOf(drinkingFrequency))) {
            adjusted.compute("보장범위", (k, v) -> v != null ? v + 0.05 : 0.05);        // 간질환 등 대비
            adjusted.compute("자기부담금수준", (k, v) -> v != null ? v + 0.03 : 0.03);  // 고위험 반영
        } else if ("0".equalsIgnoreCase(String.valueOf(drinkingFrequency))) {
            adjusted.compute("가격_경쟁력", (k, v) -> v != null ? v + 0.05 : 0.05);     // 건강군 우대
            adjusted.compute("환급범위", (k, v) -> v != null ? v + 0.03 : 0.03);
        }


        // 정규화 (합 = 1)
        return normalizeWeights(adjusted);
    }

    @Override
    public Map<Integer, double[]> getPersonaWeights() {
        Map<Integer, double[]> result = new HashMap<>();
        PERSONA_NAMES.forEach((id, name) -> {
            Map<String, Double> weightsMap = PERSONA_WEIGHTS_INSURANCE.get(name);
            if (weightsMap != null) {
                double[] weightsArray = new double[5]; // 가격, 보장한도, 보장범위, 자기부담금, 환급범위
                weightsArray[0] = weightsMap.getOrDefault("가격_경쟁력", 0.0);
                weightsArray[1] = weightsMap.getOrDefault("보장한도", 0.0);
                weightsArray[2] = weightsMap.getOrDefault("보장범위", 0.0);
                weightsArray[3] = weightsMap.getOrDefault("자기부담금", 0.0);
                weightsArray[4] = weightsMap.getOrDefault("환급범위", 0.0);
                result.put(id, weightsArray);
            }
        });
        return result;
    }

    @Override
    public double calculateScore(InsuranceProductVO product, double[] weights) {
        // weights 배열의 순서: 가격 경쟁력, 보장한도, 보장범위, 자기부담금 수준, 환급범위
        double score = 0.0;
        score += weights[0] * (product.getScorePriceCompetitiveness());
        score += weights[1] * (product.getScoreCoverageLimit());
        score += weights[2] * (product.getScoreCoverageScope());
        score += weights[3] * (product.getScoreDeductibleLevel());
        score += weights[4] * (product.getScoreRefundScope());
        return score; // 100점 만점으로 조정
    }

    // 가중치 정규화
    public Map<String, Double> normalizeWeights(Map<String, Double> weights) {
        double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum == 0) {
            return weights;
        }
        return weights.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / sum));
    }

    // Map<String, Double> 형태의 가중치를 double[] 형태로 변환
    @Override
    public double[] convertWeightsMapToArray(Map<String, Double> weightsMap) {
        double[] weightsArray = new double[5]; // 가격, 보장한도, 보장범위, 자기부담금, 환급범위
        weightsArray[0] = weightsMap.getOrDefault("가격_경쟁력", 0.0);
        weightsArray[1] = weightsMap.getOrDefault("보장한도", 0.0);
        weightsArray[2] = weightsMap.getOrDefault("보장범위", 0.0);
        weightsArray[3] = weightsMap.getOrDefault("자기부담금", 0.0);
        weightsArray[4] = weightsMap.getOrDefault("환급범위", 0.0);
        return weightsArray;
    }

    // 내부 클래스: 상품과 점수
    private static class ProductWithScore<T> {
        T product;
        double score;

        ProductWithScore(T product, double score) {
            this.product = product;
            this.score = score;
        }
    }
}