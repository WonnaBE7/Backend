//package com.wonnabe.product.service;
//
//import com.wonnabe.product.domain.SavingsProductVO;
//import com.wonnabe.product.domain.UserInfoVO;
//import com.wonnabe.product.mapper.SavingsRecommendationMapper;
//import com.wonnabe.product.mapper.UserSavingsMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class SavingsRecommendationServiceImpl implements SavingsRecommendationService {
//
//    @Autowired
//    private SavingsRecommendationMapper recommendationMapper;
//
//    @Autowired
//    private UserSavingsMapper userSavingsMapper;  // 기존 mapper 재사용
//
//    // 페르소나별 기본 가중치 (12개)
//    private static final Map<Integer, double[]> PERSONA_WEIGHTS = Map.of(
//            1, new double[]{0.4, 0.3, 0.1, 0.1, 0.1},
//            2, new double[]{0.3, 0.2, 0.3, 0.1, 0.1},
//            3, new double[]{0.2, 0.1, 0.4, 0.1, 0.2},
//            4, new double[]{0.25, 0.2, 0.35, 0.1, 0.1},
//            5, new double[]{0.3, 0.2, 0.2, 0.1, 0.2},
//            6, new double[]{0.2, 0.1, 0.4, 0.1, 0.2},
//            7, new double[]{0.35, 0.25, 0.15, 0.15, 0.1},
//            8, new double[]{0.3, 0.2, 0.3, 0.1, 0.1},
//            9, new double[]{0.3, 0.3, 0.2, 0.1, 0.1},
//            10, new double[]{0.3, 0.2, 0.2, 0.1, 0.2}
//            // 11, 12번도 추가
//    );
//
//    @Override
//    public SavingsRecommendationResponseDto recommendSavings(String userId, int topN) {
//        // 1. 사용자 정보 조회
//        UserInfoVO userInfo = recommendationMapper.getUserInfo(userId);
//
//        // 2. 모든 적금 상품과 점수 조회
//        List<SavingsProductVO> products = userSavingsMapper.getAllSavingsProducts();
//        List<SavingsProductVO> scores = recommendationMapper.getAllSavingsScores();
//
//        // 3. 상품ID로 빠른 조회를 위한 Map 생성
//        Map<String, SavingsProductVO> productMap = products.stream()
//                .collect(Collectors.toMap(SavingsProductVO::getProductId, p -> p));
//
//        // 4. 결과 객체 생성
//        SavingsRecommendationResponseDto response = new SavingsRecommendationResponseDto();
//        response.setUserId(userId);
//        response.setRecommendationsByPersona(new ArrayList<>());
//
//        // 5. 각 페르소나별로 추천
//        for (Integer personaId : userInfo.getPersonaIds()) {
//            // 기본 가중치 가져오기
//            double[] baseWeights = PERSONA_WEIGHTS.get(personaId).clone();
//
//            // 소득/고용상태로 가중치 조정
//            double[] adjustedWeights = adjustWeightsByIncome(
//                    baseWeights,
//                    userInfo.getIncomeSourceType(),
//                    userInfo.getIncomeEmploymentStatus()
//            );
//
//            // 각 상품의 점수 계산
//            List<ProductWithScore> scoredProducts = new ArrayList<>();
//            for (SavingsProductVO score : scores) {
//                double totalScore = calculateScore(score, adjustedWeights);
//                scoredProducts.add(new ProductWithScore(score.getProductId(), totalScore));
//            }
//
//            // 점수 순으로 정렬
//            scoredProducts.sort((a, b) -> Double.compare(b.score, a.score));
//
//            // 상위 N개 선택
//            PersonaRecommendation personaRec = new PersonaRecommendation();
//            personaRec.setPersonaId(personaId);
//            personaRec.setProducts(new ArrayList<>());
//
//            for (int i = 0; i < Math.min(topN, scoredProducts.size()); i++) {
//                ProductWithScore item = scoredProducts.get(i);
//                SavingsProductVO product = productMap.get(item.productId);
//
//                if (product != null) {
//                    RecommendedSavings rec = new RecommendedSavings();
//                    rec.setProductId(product.getProductId());
//                    rec.setProductName(product.getProductName());
//                    rec.setBankName(product.getBankName());
//                    rec.setBaseRate(product.getBaseRate());
//                    rec.setMaxRate(product.getMaxRate());
//                    rec.setTotalScore(item.score);
//
//                    personaRec.getProducts().add(rec);
//                }
//            }
//
//            response.getRecommendationsByPersona().add(personaRec);
//        }
//
//        return response;
//    }
//
//    // 소득/고용상태에 따른 가중치 조정
//    private double[] adjustWeightsByIncome(double[] weights, String incomeSource, String employment) {
//        double[] adjusted = weights.clone();
//
//        // 소득원별 조정
//        switch (incomeSource) {
//            case "급여":
//                adjusted[0] += 0.05;  // 금리
//                adjusted[1] += 0.05;  // 단복리
//                break;
//            case "사업":
//                adjusted[2] += 0.05;  // 우대조건
//                break;
//            case "프리랜스":
//                adjusted[3] += 0.05;  // 중도해지
//                break;
//            case "기타":
//                adjusted[4] += 0.05;  // 최대한도
//                break;
//        }
//
//        // 고용상태별 조정
//        switch (employment) {
//            case "정규직":
//                adjusted[0] += 0.05;  // 금리
//                break;
//            case "학생":
//                adjusted[4] += 0.05;  // 최대한도
//                break;
//            case "무직":
//                adjusted[3] += 0.05;  // 중도해지
//                break;
//        }
//
//        // 정규화 (합 = 1)
//        return normalizeWeights(adjusted);
//    }
//
//    // 점수 계산
//    private double calculateScore(SavingsProductVO score, double[] weights) {
//        return weights[0] * score.getInterestRateScore() +
//                weights[1] * score.getCompoundInterestScore() +
//                weights[2] * score.getPreferentialScore() +
//                weights[3] * score.getPenaltyScore() +
//                weights[4] * score.getLimitScore();
//    }
//
//    // 가중치 정규화
//    private double[] normalizeWeights(double[] weights) {
//        double sum = Arrays.stream(weights).sum();
//        return Arrays.stream(weights).map(w -> w / sum).toArray();
//    }
//
//    // 내부 클래스: 상품ID와 점수
//    private static class ProductWithScore {
//        String productId;
//        double score;
//
//        ProductWithScore(String productId, double score) {
//            this.productId = productId;
//            this.score = score;
//        }
//    }
//}