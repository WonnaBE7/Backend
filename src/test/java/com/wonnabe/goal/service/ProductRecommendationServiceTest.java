package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.GoalCreateRequestDTO;
import com.wonnabe.goal.mapper.GoalMapper;
import com.wonnabe.product.domain.SavingsProductVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductRecommendationService 테스트")
class ProductRecommendationServiceTest {

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private FinancialCalculationService calculationService;

    @InjectMocks
    private ProductRecommendationServiceImpl recommendationService;

    private GoalCreateRequestDTO request;
    private Long goalId = 1L;

    @BeforeEach
    void setUp() {
        request = GoalCreateRequestDTO.builder()
                .targetAmount(new BigDecimal("10000000"))
                .goalDurationMonths(12)
                .build();
    }

    @Test
    @DisplayName("상품 추천 시 필터링, 계산, 정렬이 정확하게 동작하는지 검증")
    void calculateRecommendations_FiltersSortsAndCalculatesCorrectly() {
        // Arrange
        List<SavingsProductVO> products = List.of(
                // C 예금 (단리 6%): 금리 1위
                SavingsProductVO.builder().productId(1501L).productName("C Deposit").bankName("Bank C").rateType("단리").maxRate(6.0F).minJoinPeriod(12).maxJoinPeriod(12).minAmount(100000L).maxAmount(10000000L).build(),
                // D 예금 (복리 5.5%): 금리 2위
                SavingsProductVO.builder().productId(1502L).productName("D Deposit").bankName("Bank D").rateType("복리").maxRate(5.5F).minJoinPeriod(12).maxJoinPeriod(36).minAmount(100000L).maxAmount(20000000L).build(),
                // A 적금 (단리 5%): 금리 3위
                SavingsProductVO.builder().productId(1001L).productName("A Savings").bankName("Bank A").rateType("단리").maxRate(5.0F).minJoinPeriod(12).maxJoinPeriod(24).minAmount(10000L).maxAmount(1000000L).build(),
                // B 적금 (복리 4.8%): 금리 4위
                SavingsProductVO.builder().productId(1002L).productName("B Savings").bankName("Bank B").rateType("복리").maxRate(4.8F).minJoinPeriod(6).maxJoinPeriod(12).minAmount(10000L).maxAmount(1000000L).build(),
                // E 적금 (기간 미달): goalDurationMonths(12) < minJoinPeriod(13) 이므로 필터링 대상
                SavingsProductVO.builder().productId(1003L).productName("E Savings").bankName("Bank E").rateType("단리").maxRate(10.0F).minJoinPeriod(13).maxJoinPeriod(24).build(),
                // F 적금 (월납입 한도 초과): 금리(7%)는 높지만, 계산된 월납입액이 maxAmount(500,000원)를 초과하여 필터링 대상
                SavingsProductVO.builder().productId(1004L).productName("F Savings").bankName("Bank F").rateType("단리").maxRate(7.0F).minJoinPeriod(12).maxJoinPeriod(12).minAmount(10000L).maxAmount(500000L).build()
        );

        when(goalMapper.getSavingsProductList()).thenReturn(products);

        // FinancialCalculationService의 메소드 호출을 Mocking

        BigDecimal rateC = BigDecimal.valueOf(6.0F).divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        BigDecimal rateD = BigDecimal.valueOf(5.5F).divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        BigDecimal rateA = BigDecimal.valueOf(5.0F).divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        BigDecimal rateB = BigDecimal.valueOf(4.8F).divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        BigDecimal rateF = BigDecimal.valueOf(7.0F).divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

        // C Deposit (단리 예금) - 1501 (예금 상품)
        when(calculationService.calculatePrincipalSimple(eq(request.getTargetAmount()), eq(12), eq(rateC)))
                .thenReturn(new BigDecimal("9433963"));
        when(calculationService.calculateFinalAmountForDeposit(eq(new BigDecimal("9433963")), eq(12), eq(rateC), eq("단리")))
                .thenReturn(new BigDecimal("10000000.78"));

        // D Deposit (복리 예금) - 1502 (예금 상품)
        when(calculationService.calculatePrincipalCompound(eq(request.getTargetAmount()), eq(12), eq(rateD)))
                .thenReturn(new BigDecimal("9478673"));
        when(calculationService.calculateFinalAmountForDeposit(eq(new BigDecimal("9478673")), eq(12), eq(rateD), eq("복리")))
                .thenReturn(new BigDecimal("10000000.015"));

        // A Savings (단리 적금) - 1001 (적금 상품)
        when(calculationService.calculateMonthlyDepositSimple(eq(request.getTargetAmount()), eq(12), eq(rateA)))
                .thenReturn(new BigDecimal("811360"));
        when(calculationService.calculateFinalAmountForSaving(eq(new BigDecimal("811360")), eq(12), eq(rateA), eq("단리")))
                .thenReturn(new BigDecimal("10000008.00"));

        // B Savings (복리 적금) - 1002 (적금 상품)
        when(calculationService.calculateMonthlyDepositCompound(eq(request.getTargetAmount()), eq(12), eq(rateB)))
                .thenReturn(new BigDecimal("815160"));
        when(calculationService.calculateFinalAmountForSaving(eq(new BigDecimal("815160")), eq(12), eq(rateB), eq("복리")))
                .thenReturn(new BigDecimal("10000007.28"));

        // F Savings (한도 초과될 상품) - 1004 (적금 상품)
        when(calculationService.calculateMonthlyDepositSimple(eq(request.getTargetAmount()), eq(12), eq(rateF)))
                .thenReturn(new BigDecimal("804025")); // MaxAmount 500,000 초과

        // 공통 Mocking (달성률 계산)
        when(calculationService.calculateAchievementRate(any(BigDecimal.class), eq(request.getTargetAmount())))
                .thenReturn(new BigDecimal("100.0000"));

        // Act
        List<RecommendedProductVO> recommendations = recommendationService.calculateRecommendations(request, goalId);

        // Assert
        assertThat(recommendations, notNullValue());
        // E(기간 미달), F(월납입 한도 초과) 상품이 필터링되어 총 4개 상품이 추천되어야 함
        assertThat(recommendations, hasSize(4));

        // 정렬 순서 검증: 금리 (내림차순)
        List<String> productNames = recommendations.stream()
                .map(RecommendedProductVO::getProductName)
                .toList();

        assertThat(productNames, contains(
                "C Deposit", // 6.0%
                "D Deposit", // 5.5%
                "A Savings", // 5.0%
                "B Savings"  // 4.8%
        ));

        // 각 상품별 저장 금액(saveAmount) 검증
        assertThat(recommendations.get(0).getSaveAmount(), comparesEqualTo(new BigDecimal("9433963"))); // C 예금 원금
        assertThat(recommendations.get(1).getSaveAmount(), comparesEqualTo(new BigDecimal("9478673"))); // D 예금 원금
        assertThat(recommendations.get(2).getSaveAmount(), comparesEqualTo(new BigDecimal("811360"))); // A 적금 월납입액
        assertThat(recommendations.get(3).getSaveAmount(), comparesEqualTo(new BigDecimal("815160"))); // B 적금 월납입액
    }
}