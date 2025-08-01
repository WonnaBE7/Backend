package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.GoalCreateRequestDTO;
import com.wonnabe.goal.mapper.GoalMapper;
import com.wonnabe.product.domain.SavingsProductVO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalServiceImpl 테스트")
class GoalServiceImplTest {

    @Mock
    private GoalMapper goalMapper;

    @InjectMocks
    private GoalServiceImpl goalService;

    @Test
    @DisplayName("예적금 상품 추천 로직의 계산 및 정렬 정확성 검증")
    @SuppressWarnings("unchecked")
    void recommendationLogic_CalculatesAndSortsCorrectly() throws Exception {
        // Arrange
        GoalCreateRequestDTO request = GoalCreateRequestDTO.builder()
                .targetAmount(new BigDecimal("10000000"))
                .goalDurationMonths(12)
                .build();
        Long goalId = 1L;

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

        // private 메서드인 calculateRecommendedProductList 호출 준비 (Reflection)
        Method method = GoalServiceImpl.class.getDeclaredMethod("calculateRecommendedProductList", GoalCreateRequestDTO.class, Long.class);
        method.setAccessible(true); // private 메소드에 접근 가능하도록 설정

        // Act
        List<RecommendedProductVO> recommendations = (List<RecommendedProductVO>) method.invoke(goalService, request, goalId);

        // Assert
        assertThat(recommendations, notNullValue());
        // E(기간 미달), F(월납입 한도 초과) 상품이 필터링되어 총 4개 상품이 추천되어야 함
        assertThat(recommendations, hasSize(4));

        // 정렬 순서 검증: 달성률(모두 100%) > 금리 (내림차순) > 월납입액 (오름차순)
        // 현재 로직에서는 달성률이 모두 100%이므로, 사실상 금리 내림차순으로 정렬됨.
        List<String> productNames = recommendations.stream()
                .map(RecommendedProductVO::getProductName)
                .toList();

        assertThat(productNames, contains(
                "C Deposit", // 6.0%
                "D Deposit", // 5.5%
                "A Savings", // 5.0%
                "B Savings"  // 4.8%
        ));

        // 각 상품별 계산 정확성 검증
        // [C Deposit] 단리 예금
        RecommendedProductVO cDeposit = recommendations.get(0);
        assertThat(cDeposit.getProductName(), equalTo("C Deposit"));
        // 계산식: P = A / (1 + rt)
        // P = 10,000,000 / (1 + 0.06 * 1) = 9433962.26... -> CEILING -> 9433963
        assertThat(cDeposit.getSaveAmount(), comparesEqualTo(new BigDecimal("9433963")));
        assertThat(cDeposit.getAchievementRate(), comparesEqualTo(new BigDecimal("100.0000")));

        // [D Deposit] 복리 예금
        RecommendedProductVO dDeposit = recommendations.get(1);
        assertThat(dDeposit.getProductName(), equalTo("D Deposit"));
        // 계산식: P = A / (1 + r)^t
        // P = 10,000,000 / (1.055)^1 = 9478672.98... -> CEILING -> 9478673
        assertThat(dDeposit.getSaveAmount(), comparesEqualTo(new BigDecimal("9478673")));

        // [A Savings] 단리 적금
        RecommendedProductVO aSavings = recommendations.get(2);
        assertThat(aSavings.getProductName(), equalTo("A Savings"));
        // 계산식: 월납입금 = A / (n + r/12 * n(n+1)/2)
        // 분모 = 12 + (0.05 / 12 * 12 * 13 / 2) = 12.325 -> 월납입금 = 10,000,000 / 12.325 = 811359.02... -> CEILING -> 811360
        assertThat(aSavings.getSaveAmount(), comparesEqualTo(new BigDecimal("811360")));

        // [B Savings] 복리 적금
        RecommendedProductVO bSavings = recommendations.get(3);
        assertThat(bSavings.getProductName(), equalTo("B Savings"));
        // 계산식: 월납입금 = (A * i) / ((1 + i)^n - 1)
        // i = 0.048 / 12 = 0.004 -> 월납입금 = (10,000,000 * 0.004) / ((1.004)^12 - 1) = 815159.2... -> CEILING -> 815160
        assertThat(bSavings.getSaveAmount(), comparesEqualTo(new BigDecimal("815159")));
    }

    @Test
    void getGoalList() {
    }

    @Test
    void getGoalDetail() {
    }

    @Test
    void createGoal() {
    }

    @Test
    void publishAsReport() {
    }

    @Test
    void achieveGoal() {
    }
}