package com.wonnabe.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserInsuranceVO;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.BasicUserInfo;
import com.wonnabe.product.dto.InsuranceProductDetailResponseDTO;
import com.wonnabe.product.dto.SavingsProductDetailResponseDto;
import com.wonnabe.product.mapper.ProductDetailMapper;
import com.wonnabe.product.mapper.UserInsuranceMapper;
import com.wonnabe.product.mapper.UserSavingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    @Override
    public SavingsProductDetailResponseDto getSavingProductDetail(String productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // UserDetails를 CustomUser로 형 변환합니다.
        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        // CustomUser가 가지고 있는 UserVO 객체에서 직접 userId(UUID)를 가져옵니다.
        String userId = customUser.getUser().getUserId();

        BasicUserInfo basicUserInfo = productDetailMapper.findBasicUserInfoById(userId);
        SavingsProductVO product = productDetailMapper.findSavingProductById(productId);

        double[] weights = savingsRecommendationService.getPersonaWeights().get(basicUserInfo.getNowMeId());
        int matchScore = (int) savingsRecommendationService.calculateScore(product, weights);

        List<Long> favoriteProductIds = Collections.emptyList();
        try {
            if (basicUserInfo.getFavoriteProductsByType() != null && !basicUserInfo.getFavoriteProductsByType().isEmpty()) {
                favoriteProductIds = objectMapper.readValue(basicUserInfo.getFavoriteProductsByType(), new TypeReference<List<Long>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing favoriteProductsByType JSON: {}", basicUserInfo.getFavoriteProductsByType(), e);
            // 예외 발생 시 찜 목록을 비어있는 리스트로 처리하거나 다른 적절한 방식으로 처리
        }
        boolean isWished = favoriteProductIds.contains(Long.valueOf(productId));

        SavingsProductDetailResponseDto.ProductInfo productInfo = SavingsProductDetailResponseDto.ProductInfo.builder()
                .productId(String.valueOf(product.getProductId()))
                .productName(product.getProductName())
                .bankName(product.getBankName())
                .matchScore(matchScore)
                .interestRate("연 " + product.getBaseRate() + "%")
                .maxInterestRate("연 " + product.getMaxRate() + "%")
                .benefitSummary(product.getPrefer())
                .isWished(isWished)
                .labels(Arrays.asList("금리", "단/복리", "우대조건", "중도해지 페널티", "최대한도"))
                .currentUserData(Arrays.asList(product.getInterestRateScore(), product.getCompoundInterestScore(), product.getPreferentialScore(), product.getPenaltyScore(), product.getLimitScore()))
                .build();

        List<UserSavingsVO> userSavings = userSavingsMapper.findAllByUserId(userId);

        List<SavingsProductDetailResponseDto.ComparisonChart> comparisonChart = userSavings.stream().map(saving -> {
            SavingsProductVO savingProduct = productDetailMapper.findSavingProductById(String.valueOf(saving.getProductId()));
            return SavingsProductDetailResponseDto.ComparisonChart.builder()
                    .compareId(saving.getId())
                    .compareName(savingProduct.getProductName())
                    .recommendedProductData(Arrays.asList(savingProduct.getInterestRateScore(), savingProduct.getCompoundInterestScore(), savingProduct.getPreferentialScore(), savingProduct.getPenaltyScore(), savingProduct.getLimitScore()))
                    .build();
        }).collect(Collectors.toList());

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
    public InsuranceProductDetailResponseDTO getInsuranceProductDetail(String productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        String userId = customUser.getUser().getUserId();

        BasicUserInfo basicUserInfo = productDetailMapper.findBasicUserInfoById(userId);
        InsuranceProductVO product = productDetailMapper.findInsuranceProductById(productId);

        // matchScore 계산
        double[] weights = insuranceRecommendationService.getPersonaWeights().get(basicUserInfo.getNowMeId());
        int matchScore = (int) insuranceRecommendationService.calculateScore(product, weights);

        // averagePremium 계산
        BigDecimal femalePremium = product.getFemalePremium() != null ? product.getFemalePremium() : BigDecimal.ZERO;
        BigDecimal malePremium = product.getMalePremium() != null ? product.getMalePremium() : BigDecimal.ZERO;
        BigDecimal averagePremium = femalePremium.add(malePremium).divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP); // 소수점 첫째자리에서 반올림

        // isWished 확인
        List<Long> favoriteProductIds = Collections.emptyList();
        try {
            if (basicUserInfo.getFavoriteProductsByType() != null && !basicUserInfo.getFavoriteProductsByType().isEmpty()) {
                favoriteProductIds = objectMapper.readValue(basicUserInfo.getFavoriteProductsByType(), new TypeReference<List<Long>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing favoriteProductsByType JSON for insurance: {}", basicUserInfo.getFavoriteProductsByType(), e);
        }
        boolean isWished = favoriteProductIds.contains(Long.valueOf(productId));

        InsuranceProductDetailResponseDTO.ProductInfo productInfo = InsuranceProductDetailResponseDTO.ProductInfo.builder()
                .productId(String.valueOf(product.getProductId()))
                .productName(product.getProductName())
                .providerName(product.getProviderName())
                .matchScore(matchScore)
                .coverageType(product.getCoverageType())
                .coverageLimit(product.getCoverageLimit())
                .deductible(product.getDeductibleScore() != null ? String.valueOf(product.getDeductibleScore()) : null) // 자기부담금 점수를 문자열로 변환
                .averagePremium(averagePremium.toPlainString() + "원")
                .isWished(isWished)
                .labels(Arrays.asList("가격 경쟁력", "보장한도", "보장범위", "자기부담금 수준", "환급범위"))
                .currentUserData(Arrays.asList(
                        product.getPriceCompetitivenessScore() != null ? product.getPriceCompetitivenessScore().intValue() : 0,
                        product.getCoverageLimitScore() != null ? product.getCoverageLimitScore().intValue() : 0,
                        product.getCoverageScopeScore() != null ? product.getCoverageScopeScore().intValue() : 0,
                        product.getDeductibleScore() != null ? product.getDeductibleScore().intValue() : 0,
                        product.getRefundScopeScore() != null ? product.getRefundScopeScore().intValue() : 0
                ))
                .build();

        List<UserInsuranceVO> userInsurances = userInsuranceMapper.findAllByUserId(userId);

        List<InsuranceProductDetailResponseDTO.ComparisonChart> comparisonChart = userInsurances.stream().map(insurance -> {
            InsuranceProductVO insuranceProduct = productDetailMapper.findInsuranceProductById(String.valueOf(insurance.getProductId()));
            return InsuranceProductDetailResponseDTO.ComparisonChart.builder()
                    .compareId(insurance.getId())
                    .compareName(insuranceProduct.getProductName())
                    .recommendedProductData(Arrays.asList(
                            insuranceProduct.getPriceCompetitivenessScore() != null ? insuranceProduct.getPriceCompetitivenessScore().intValue() : 0,
                            insuranceProduct.getCoverageLimitScore() != null ? insuranceProduct.getCoverageLimitScore().intValue() : 0,
                            insuranceProduct.getCoverageScopeScore() != null ? insuranceProduct.getCoverageScopeScore().intValue() : 0,
                            insuranceProduct.getDeductibleScore() != null ? insuranceProduct.getDeductibleScore().intValue() : 0,
                            insuranceProduct.getRefundScopeScore() != null ? insuranceProduct.getRefundScopeScore().intValue() : 0
                    ))
                    .build();
        }).collect(Collectors.toList());

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
