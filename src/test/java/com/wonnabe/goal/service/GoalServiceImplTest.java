package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.GoalVO;
import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.*;
import com.wonnabe.goal.mapper.GoalMapper;
import com.wonnabe.product.domain.SavingsProductVO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalServiceImpl 테스트")
class GoalServiceImplTest {

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private OpenAiService openAiService;

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
    @DisplayName("목표 목록 조회 - PUBLISHED 상태")
    void getGoalList_Published() {
        // given
        String userId = "testUser";
        String status = "PUBLISHED";

        List<GoalSummaryResponseDTO> mockGoals = List.of(
                GoalSummaryResponseDTO.builder()
                        .id(1L)
                        .goalName("내 집 마련")
                        .targetAmount(new BigDecimal("50000000"))
                        .build(),
                GoalSummaryResponseDTO.builder()
                        .id(2L)
                        .goalName("결혼 자금")
                        .targetAmount(new BigDecimal("30000000"))
                        .build()
        );

        when(goalMapper.getGoalList(userId, status)).thenReturn(mockGoals);

        // when
        GoalListResponseDTO result = goalService.getGoalList(userId, status);

        // then
        assertThat(result.getTotalGoalCount(), equalTo(2));
        assertThat(result.getTotalTargetAmount(), comparesEqualTo(new BigDecimal("80000000")));
        assertThat(result.getGoals(), hasSize(2));
        assertThat(result.getGoals().get(0).getGoalName(), equalTo("내 집 마련"));
    }

    @Test
    @DisplayName("목표 목록 조회 - 잘못된 상태값으로 예외 발생")
    void getGoalList_InvalidStatus() {
        // given
        String userId = "testUser";
        String invalidStatus = "INVALID";

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> goalService.getGoalList(userId, invalidStatus)
        );

        assertThat(exception.getMessage(), containsString("유효하지 않은 상태 값입니다"));
    }

    @Test
    @DisplayName("목표 상세 조회 성공")
    void getGoalDetail_Success() {
        // given
        String userId = "testUser";
        Long goalId = 1L;

        GoalDetailResponseDTO mockGoalDetail = GoalDetailResponseDTO.builder()
                .id(goalId)
                .goalName("내 집 마련")
                .targetAmount(new BigDecimal("50000000"))
                .build();

        List<RecommendedProductVO> mockProducts = List.of(
                RecommendedProductVO.builder()
                        .productId(1001L)
                        .productName("Test Savings")
                        .bankName("Test Bank")
                        .build()
        );

        when(goalMapper.getGoal(userId, goalId)).thenReturn(mockGoalDetail);
        when(goalMapper.getRecommendedProductList(goalId)).thenReturn(mockProducts);

        // when
        GoalDetailResponseDTO result = goalService.getGoalDetail(userId, goalId);

        // then
        assertThat(result.getId(), equalTo(goalId));
        assertThat(result.getGoalName(), equalTo("내 집 마련"));
        assertThat(result.getRecommendedProducts(), hasSize(1));
    }

    @Test
    @DisplayName("목표 상세 조회 - 존재하지 않는 목표로 예외 발생")
    void getGoalDetail_NotFound() {
        // given
        String userId = "testUser";
        Long goalId = 999L;

        when(goalMapper.getGoal(userId, goalId)).thenReturn(null);

        // when & then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> goalService.getGoalDetail(userId, goalId)
        );

        assertThat(exception.getMessage(), containsString("요청하신 목표 (ID: 999)를 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("목표 생성 성공")
    void createGoal_Success() {
        // given
        String userId = "testUser";
        GoalCreateRequestDTO request = GoalCreateRequestDTO.builder()
                .categoryId(1)
                .goalName("내 집 마련")
                .targetAmount(new BigDecimal("50000000"))
                .goalDurationMonths(24)
                .build();

        Integer nowmeId = 1;
        String nowmeName = "절약왕";
        String nowmeDescription = "꼼꼼하게 절약하는 성향";
        String categoryName = "주거";
        String mockGptResponse = "안녕, 과거의 나야! 내 집 마련 목표를 달성했어!";

        // Mock 설정
        when(goalMapper.getNowmeIdByUserId(userId)).thenReturn(nowmeId);
        when(goalMapper.getNowmeNameByNowmeId(nowmeId)).thenReturn(nowmeName);
        when(goalMapper.getNowmeDescriptionByNowmeId(nowmeId)).thenReturn(nowmeDescription);
        when(goalMapper.getCategoryNameById(request.getCategoryId())).thenReturn(categoryName);
        when(openAiService.getGptResponse(anyString())).thenReturn(mockGptResponse);
        when(goalMapper.getSavingsProductList()).thenReturn(List.of());
        when(goalMapper.getRecommendedProductList(anyLong())).thenReturn(List.of());

        // GoalVO 캡처를 위한 ArgumentCaptor
        ArgumentCaptor<GoalVO> goalCaptor = ArgumentCaptor.forClass(GoalVO.class);
        doAnswer(invocation -> {
            GoalVO goal = invocation.getArgument(0);
            goal.setId(1L); // 생성된 ID 설정
            return null;
        }).when(goalMapper).createGoal(goalCaptor.capture());

        // when
        GoalCreateResponseDTO result = goalService.createGoal(userId, request);

        // then
        assertThat(result.getGoalId(), equalTo(1L));
        assertThat(result.getFutureMeMessage(), equalTo(mockGptResponse));
        assertThat(result.getRecommendedProducts(), notNullValue());

        // 저장된 GoalVO 검증
        GoalVO savedGoal = goalCaptor.getValue();
        assertThat(savedGoal.getUserId(), equalTo(userId));
        assertThat(savedGoal.getGoalName(), equalTo("내 집 마련"));
        assertThat(savedGoal.getTargetAmount(), comparesEqualTo(new BigDecimal("50000000")));
        assertThat(savedGoal.getStatus(), equalTo("DRAFT"));
    }

    @Test
    @DisplayName("목표 생성 - 존재하지 않는 카테고리로 예외 발생")
    void createGoal_InvalidCategory() {
        // given
        String userId = "testUser";
        GoalCreateRequestDTO request = GoalCreateRequestDTO.builder()
                .categoryId(999)
                .goalName("내 집 마련")
                .targetAmount(new BigDecimal("50000000"))
                .goalDurationMonths(24)
                .build();

        when(goalMapper.getNowmeIdByUserId(userId)).thenReturn(1);
        when(goalMapper.getCategoryNameById(request.getCategoryId())).thenReturn(null);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> goalService.createGoal(userId, request)
        );

        assertThat(exception.getMessage(), containsString("해당하는 카테고리가 존재하지 않습니다"));
    }

    @Test
    @DisplayName("목표 발행 성공")
    void publishAsReport_Success() {
        // given
        String userId = "testUser";
        Long goalId = 1L;
        Long selectedProductId = 1001L;

        GoalDetailResponseDTO existingGoal = GoalDetailResponseDTO.builder()
                .id(goalId)
                .goalName("내 집 마련")
                .build();

        RecommendedProductVO selectedProduct = RecommendedProductVO.builder()
                .productId(selectedProductId)
                .saveAmount(new BigDecimal("800000"))
                .expectedTotalAmount(new BigDecimal("50000000"))
                .build();

        GoalSummaryResponseDTO publishedGoal = GoalSummaryResponseDTO.builder()
                .id(goalId)
                .goalName("내 집 마련")
                .status("PUBLISHED")
                .build();

        when(goalMapper.getGoal(userId, goalId)).thenReturn(existingGoal);
        when(goalMapper.findRecommendedProductById(selectedProductId, goalId)).thenReturn(selectedProduct);
        when(goalMapper.getGoalSummaryById(goalId)).thenReturn(publishedGoal);

        // when
        GoalSummaryResponseDTO result = goalService.publishAsReport(userId, goalId, selectedProductId);

        // then
        assertThat(result.getId(), equalTo(goalId));
        assertThat(result.getStatus(), equalTo("PUBLISHED"));

        // updateGoalStatusToPublished 호출 검증
        verify(goalMapper).updateGoalStatusToPublished(
                eq(goalId),
                eq(selectedProductId),
                eq(selectedProduct.getSaveAmount()),
                eq(selectedProduct.getExpectedTotalAmount())
        );
    }

    @Test
    @DisplayName("목표 발행 - 존재하지 않는 목표로 예외 발생")
    void publishAsReport_GoalNotFound() {
        // given
        String userId = "testUser";
        Long goalId = 999L;
        Long selectedProductId = 1001L;

        when(goalMapper.getGoal(userId, goalId)).thenReturn(null);

        // when & then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> goalService.publishAsReport(userId, goalId, selectedProductId)
        );

        assertThat(exception.getMessage(), containsString("목표 (ID: 999)를 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("목표 발행 - 존재하지 않는 상품으로 예외 발생")
    void publishAsReport_ProductNotFound() {
        // given
        String userId = "testUser";
        Long goalId = 1L;
        Long selectedProductId = 999L;

        GoalDetailResponseDTO existingGoal = GoalDetailResponseDTO.builder()
                .id(goalId)
                .goalName("내 집 마련")
                .build();

        when(goalMapper.getGoal(userId, goalId)).thenReturn(existingGoal);
        when(goalMapper.findRecommendedProductById(selectedProductId, goalId)).thenReturn(null);

        // when & then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> goalService.publishAsReport(userId, goalId, selectedProductId)
        );

        assertThat(exception.getMessage(), containsString("해당하는 상품이 없습니다"));
    }

    @Test
    @DisplayName("목표 달성 성공")
    void achieveGoal_Success() {
        // given
        String userId = "testUser";
        Long goalId = 1L;

        GoalDetailResponseDTO existingGoal = GoalDetailResponseDTO.builder()
                .id(goalId)
                .goalName("내 집 마련")
                .build();

        GoalSummaryResponseDTO achievedGoal = GoalSummaryResponseDTO.builder()
                .id(goalId)
                .goalName("내 집 마련")
                .status("ACHIEVED")
                .build();

        when(goalMapper.getGoal(userId, goalId)).thenReturn(existingGoal);
        when(goalMapper.getGoalSummaryById(goalId)).thenReturn(achievedGoal);

        // when
        GoalSummaryResponseDTO result = goalService.achieveGoal(userId, goalId);

        // then
        assertThat(result.getId(), equalTo(goalId));
        assertThat(result.getStatus(), equalTo("ACHIEVED"));

        // updateGoalStatusToAchieved 호출 검증
        verify(goalMapper).updateGoalStatusToAchieved(eq(goalId), ArgumentMatchers.any(LocalDateTime.class));
    }

    @Test
    @DisplayName("목표 달성 - 존재하지 않는 목표로 예외 발생")
    void achieveGoal_GoalNotFound() {
        // given
        String userId = "testUser";
        Long goalId = 999L;

        when(goalMapper.getGoal(userId, goalId)).thenReturn(null);

        // when & then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> goalService.achieveGoal(userId, goalId)
        );

        assertThat(exception.getMessage(), containsString("목표 (ID: 999)를 찾을 수 없습니다"));
    }
}