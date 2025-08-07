package com.wonnabe.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.BasicUserInfo;
import com.wonnabe.product.dto.SavingsProductDetailResponseDto;
import com.wonnabe.product.mapper.ProductMapper;
import com.wonnabe.product.mapper.UserSavingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final UserSavingsMapper userSavingsMapper;
    private final SavingsRecommendationService savingsRecommendationService;
    private final ObjectMapper objectMapper;

    @Override
    public SavingsProductDetailResponseDto getSavingProductDetail(String productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // UserDetails를 CustomUser로 형 변환합니다.
        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        // CustomUser가 가지고 있는 UserVO 객체에서 직접 userId(UUID)를 가져옵니다.
        String userId = customUser.getUser().getUserId();

        BasicUserInfo basicUserInfo = productMapper.findBasicUserInfoById(userId);
        SavingsProductVO product = productMapper.findSavingProductById(productId);

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
            SavingsProductVO savingProduct = productMapper.findSavingProductById(String.valueOf(saving.getProductId()));
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
}
