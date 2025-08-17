package com.wonnabe.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.product.domain.*;
import com.wonnabe.product.dto.BasicUserInfoDTO;
import com.wonnabe.product.dto.InsuranceProductDetailResponseDTO;
import com.wonnabe.product.dto.SavingsProductDetailResponseDto;
import com.wonnabe.product.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProductDetailServiceImpl implements ProductDetailService {

    private final ProductDetailMapper productDetailMapper;
    private final UserSavingsMapper userSavingsMapper;
    private final InsuranceRecommendationService insuranceRecommendationService;
    private final UserInsuranceMapper userInsuranceMapper;
    private final SavingsRecommendationService savingsRecommendationService;
    private final ObjectMapper objectMapper;
    private final SavingsRecommendationMapper savingsRecommendationMapper; // 의존성 추가
    private final InsuranceRecommendationMapper insuranceRecommendationMapper; // 의존성 추가

    @Override
    public SavingsProductDetailResponseDto getSavingProductDetail(String productId, String userId, Integer wannabeId) {
        SavingsProductVO product = productDetailMapper.findSavingProductById(productId);
        double matchScore = 0.0;
        boolean isWished = false;
        List<SavingsProductDetailResponseDto.ComparisonChart> comparisonChart = Collections.emptyList();

        if (userId != null) {
            // 올바른 매퍼와 VO를 사용하여 사용자 정보를 조회
            UserIncomeInfoVO userInfo = savingsRecommendationMapper.getUserIncomeInfo(userId);
            BasicUserInfoDTO basicUserInfo = productDetailMapper.findBasicUserInfoById(userId); // 찜하기, 비교 목록을 위해 기존 DTO도 유지

            if (userInfo != null) {
                Integer personaIdToUse = wannabeId;
                if (personaIdToUse == null && userInfo.getPersonaIds() != null && !userInfo.getPersonaIds().isEmpty()) {
                    personaIdToUse = userInfo.getPersonaIds().get(0); // getPersonaIds()의 첫번째 값을 기본값으로 사용
                }
                if (personaIdToUse != null) {
                    double[] baseWeights = savingsRecommendationService.getPersonaWeights().get(personaIdToUse);
                    if (baseWeights != null) {
                        double[] adjustedWeights = savingsRecommendationService.adjustWeightsByIncome(baseWeights, userInfo.getIncomeSourceType(), userInfo.getIncomeEmploymentStatus());
                        double[] normalizedWeights = savingsRecommendationService.normalizeWeights(adjustedWeights);
                        matchScore = savingsRecommendationService.calculateScore(product, normalizedWeights);
                    }
                }
            }

            if (basicUserInfo != null) {
                // 찜하기 여부 확인
                List<Long> favoriteProductIds = Collections.emptyList();
                try {
                    if (basicUserInfo.getFavoriteProductsByType() != null && !basicUserInfo.getFavoriteProductsByType().isEmpty()) {
                        favoriteProductIds = objectMapper.readValue(basicUserInfo.getFavoriteProductsByType(), new TypeReference<List<Long>>() {});
                    }
                } catch (JsonProcessingException e) {
                    log.error("Error parsing favoriteProductsByType JSON: {}", basicUserInfo.getFavoriteProductsByType(), e);
                }
                isWished = favoriteProductIds.contains(Long.valueOf(productId));

                // 비교 차트 정보
                List<UserSavingsVO> userSavings = userSavingsMapper.findAllByUserId(userId);
                comparisonChart = userSavings.stream().map(saving -> {
                    SavingsProductVO savingProduct = productDetailMapper.findSavingProductById(String.valueOf(saving.getProductId()));
                    return SavingsProductDetailResponseDto.ComparisonChart.builder()
                            .compareId(saving.getId())
                            .compareName(savingProduct.getProductName())
                            .recommendedProductData(Arrays.asList(savingProduct.getScoreInterestRate(), savingProduct.getScoreInterestType(), savingProduct.getScorePreferentialCondition(), savingProduct.getScoreCancelBenefit(), savingProduct.getScoreMaxAmount()))
                            .build();
                }).collect(Collectors.toList());
            }
        } else if (wannabeId != null) {
            double[] weights = savingsRecommendationService.getPersonaWeights().get(wannabeId);
            if (weights != null) {
                matchScore = savingsRecommendationService.calculateScore(product, weights);
            }
        }

        SavingsProductDetailResponseDto.ProductInfo productInfo = SavingsProductDetailResponseDto.ProductInfo.builder()
                .productId(String.valueOf(product.getProductId()))
                .productName(product.getProductName())
                .bankName(product.getBankName())
                .score(Math.round(matchScore * 10.0) / 10.0)
                .interestRate("연 " + product.getBaseRate() + "%")
                .maxInterestRate("연 " + product.getMaxRate() + "%")
                .benefitSummary(product.getPrefer())
                .isWished(isWished)
                .labels(Arrays.asList("금리", "단/복리", "우대조건", "중도해지 페널티", "최대한도"))
                .currentUserData(Arrays.asList(product.getScoreInterestRate(), product.getScoreInterestType(), product.getScorePreferentialCondition(), product.getScoreCancelBenefit(), product.getScoreMaxAmount()))
                .build();

        List<String> maturityContent = (product.getMtrtInt() != null && !product.getMtrtInt().isEmpty()) ?
                Arrays.asList(product.getMtrtInt().split(",")) : Collections.emptyList();

        SavingsProductDetailResponseDto.MaturityInfo maturityInfo = SavingsProductDetailResponseDto.MaturityInfo.builder()
                .maxJoinPeroid(product.getMaxJoinPeriod() + "개월")
                .title("만기 이후 금리")
                .content(maturityContent)
                .build();

        return SavingsProductDetailResponseDto.builder()
                .productInfo(productInfo)
                .comparisonChart(comparisonChart)
                .maturityInfo(maturityInfo)
                .build();
    }

    @Override
    public InsuranceProductDetailResponseDTO getInsuranceProductDetail(String productId, String userId, Integer wannabeId) {
        InsuranceProductVO product = productDetailMapper.findInsuranceProductById(productId);
        double matchScore = 0.0;
        boolean isWished = false;
        List<InsuranceProductDetailResponseDTO.ComparisonChart> comparisonChart = Collections.emptyList();

        if (userId != null) {
            // 올바른 매퍼와 VO를 사용하여 사용자 정보를 조회
            UserIncomeInfoVO userInfo = insuranceRecommendationMapper.getUserHealthInfo(userId);
            BasicUserInfoDTO basicUserInfo = productDetailMapper.findBasicUserInfoById(userId); // 찜하기, 비교 목록을 위해 기존 DTO도 유지

            if (userInfo != null) {
                Integer personaIdToUse = wannabeId;
                if (personaIdToUse == null && userInfo.getPersonaIds() != null && !userInfo.getPersonaIds().isEmpty()) {
                    personaIdToUse = userInfo.getPersonaIds().get(0); // getPersonaIds()의 첫번째 값을 기본값으로 사용
                }
                if (personaIdToUse != null) {
                    Map<Integer, double[]> personaWeights = insuranceRecommendationService.getPersonaWeights();
                    double[] baseWeightsArray = personaWeights.get(personaIdToUse);
                    if (baseWeightsArray != null) {
                        int smokingStatus = userInfo.getSmokingStatus();
                        int familyMedicalHistory = userInfo.getFamilyMedicalHistory();
                        int pastMedicalHistory = userInfo.getPastMedicalHistory();
                        int exerciseFrequency = userInfo.getExerciseFrequency();
                        int drinkingFrequency = userInfo.getDrinkingFrequency();

                        Map<String, Double> baseWeightsMap = new HashMap<>();
                        baseWeightsMap.put("가격_경쟁력", baseWeightsArray[0]);
                        baseWeightsMap.put("보장한도", baseWeightsArray[1]);
                        baseWeightsMap.put("보장범위", baseWeightsArray[2]);
                        baseWeightsMap.put("자기부담금", baseWeightsArray[3]);
                        baseWeightsMap.put("환급범위", baseWeightsArray[4]);

                        Map<String, Double> adjustedWeights = insuranceRecommendationService.adjustWeightsByHealthAndLifestyle(baseWeightsMap, smokingStatus, familyMedicalHistory, pastMedicalHistory, exerciseFrequency, drinkingFrequency);
                        Map<String, Double> normalizedWeights = insuranceRecommendationService.normalizeWeights(adjustedWeights);
                        double[] finalWeights = insuranceRecommendationService.convertWeightsMapToArray(normalizedWeights);
                        matchScore = insuranceRecommendationService.calculateScore(product, finalWeights);
                    }
                }
            }

            if (basicUserInfo != null) {
                // 찜하기 여부 확인
                List<Long> favoriteProductIds = Collections.emptyList();
                try {
                    if (basicUserInfo.getFavoriteProductsByType() != null && !basicUserInfo.getFavoriteProductsByType().isEmpty()) {
                        favoriteProductIds = objectMapper.readValue(basicUserInfo.getFavoriteProductsByType(), new TypeReference<List<Long>>() {});
                    }
                } catch (JsonProcessingException e) {
                    log.error("Error parsing favoriteProductsByType JSON for insurance: {}", basicUserInfo.getFavoriteProductsByType(), e);
                }
                isWished = favoriteProductIds.contains(Long.valueOf(productId));

                // 비교 차트 정보
                List<UserInsuranceVO> userInsurances = userInsuranceMapper.findAllByUserId(userId);
                comparisonChart = userInsurances.stream().map(insurance -> {
                    InsuranceProductVO insuranceProduct = productDetailMapper.findInsuranceProductById(String.valueOf(insurance.getProductId()));
                    return InsuranceProductDetailResponseDTO.ComparisonChart.builder()
                            .compareId(insurance.getId())
                            .compareName(insuranceProduct.getProductName())
                            .recommendedProductData(Arrays.asList(
                                    insuranceProduct.getScorePriceCompetitiveness(),
                                    insuranceProduct.getScoreCoverageLimit(),
                                    insuranceProduct.getScoreCoverageScope(),
                                    insuranceProduct.getScoreDeductibleLevel(),
                                    insuranceProduct.getScoreRefundScope()
                            ))
                            .build();
                }).collect(Collectors.toList());
            }
        } else if (wannabeId != null) {
            double[] weights = insuranceRecommendationService.getPersonaWeights().get(wannabeId);
            if (weights != null) {
                matchScore = insuranceRecommendationService.calculateScore(product, weights);
            }
        }

        BigDecimal femalePremium = product.getFemalePremium() != null ? product.getFemalePremium() : BigDecimal.ZERO;
        BigDecimal malePremium = product.getMalePremium() != null ? product.getMalePremium() : BigDecimal.ZERO;
        BigDecimal averagePremium = femalePremium.add(malePremium).divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);

        InsuranceProductDetailResponseDTO.ProductInfo productInfo = InsuranceProductDetailResponseDTO.ProductInfo.builder()
                .productId(String.valueOf(product.getProductId()))
                .productName(product.getProductName())
                .providerName(product.getProviderName())
                .score(Math.round(matchScore * 10.0) / 10.0)
                .coverageType(product.getCoverageType())
                .coverageLimit(product.getCoverageLimit())
                .deductible(String.valueOf(product.getScoreDeductibleLevel()))
                .averagePremium(averagePremium.toPlainString())
                .isWished(isWished)
                .labels(Arrays.asList("가격 경쟁력", "보장한도", "보장범위", "자기부담금 수준", "환급범위"))
                .currentUserData(Arrays.asList(
                        product.getScorePriceCompetitiveness(),
                        product.getScoreCoverageLimit(),
                        product.getScoreCoverageScope(),
                        product.getScoreDeductibleLevel(),
                        product.getScoreRefundScope()
                ))
                .build();

        InsuranceProductDetailResponseDTO.MaturityInfo maturityInfo = InsuranceProductDetailResponseDTO.MaturityInfo.builder()
                .coverageDesc(product.getCoverageDesc())
                .note(product.getNote())
                .build();

        return InsuranceProductDetailResponseDTO.builder()
                .productInfo(productInfo)
                .comparisonChart(comparisonChart)
                .maturityInfo(maturityInfo)
                .build();
    }
}