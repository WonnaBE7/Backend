package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.GoalVO;
import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.*;
import com.wonnabe.goal.mapper.GoalMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalServiceImpl 테스트")
class GoalServiceImplTest {

    @Mock
    private GoalMapper goalMapper;
    @Mock
    private FinancialCalculationService calculationService;
    @Mock
    private ProductRecommendationService recommendationService;
    @Mock
    private FutureMessageService messageService;
    @Mock
    private GoalValidationService validationService;

    @InjectMocks
    private GoalServiceImpl goalService;

    @Test
    @DisplayName("목표 목록 조회 - PUBLISHED 상태")
    void getGoalList_Published() {
        // Given
        String userId = "testUser";
        String status = "PUBLISHED";

        List<GoalSummaryResponseDTO> mockGoals = List.of(
                GoalSummaryResponseDTO.builder().id(1L).goalName("내 집 마련").targetAmount(new BigDecimal("50000000")).build(),
                GoalSummaryResponseDTO.builder().id(2L).goalName("결혼 자금").targetAmount(new BigDecimal("30000000")).build()
        );

        doNothing().when(validationService).validateGoalStatus(status);
        when(goalMapper.getGoalList(userId, status)).thenReturn(mockGoals);

        // When
        GoalListResponseDTO result = goalService.getGoalList(userId, status);

        // Then
        verify(validationService).validateGoalStatus(status); // 검증 로직 호출 확인
        assertThat(result.getTotalGoalCount(), equalTo(2));
        assertThat(result.getTotalTargetAmount(), comparesEqualTo(new BigDecimal("80000000")));
        assertThat(result.getGoals(), hasSize(2));
    }

    @Test
    @DisplayName("목표 목록 조회 - 잘못된 상태값으로 예외 발생")
    void getGoalList_InvalidStatus() {
        // Given
        String userId = "testUser";
        String invalidStatus = "INVALID";

        // 검증 서비스가 예외를 던지도록 설정
        doThrow(new IllegalArgumentException("유효하지 않은 상태 값입니다."))
                .when(validationService).validateGoalStatus(invalidStatus);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> goalService.getGoalList(userId, invalidStatus)
        );

        assertThat(exception.getMessage(), containsString("유효하지 않은 상태 값입니다"));
    }

    @Test
    @DisplayName("목표 상세 조회 성공")
    void getGoalDetail_Success() {
        // Given
        String userId = "testUser";
        Long goalId = 1L;

        GoalDetailResponseDTO mockGoalDetail = GoalDetailResponseDTO.builder().id(goalId).goalName("내 집 마련").build();
        List<RecommendedProductVO> mockProducts = List.of(RecommendedProductVO.builder().productId(1001L).build());

        // 검증 서비스가 목표 객체를 반환하도록 설정
        when(validationService.validateGoalExists(userId, goalId)).thenReturn(mockGoalDetail);
        when(goalMapper.getRecommendedProductList(goalId)).thenReturn(mockProducts);

        // When
        GoalDetailResponseDTO result = goalService.getGoalDetail(userId, goalId);

        // Then
        assertThat(result.getId(), equalTo(goalId));
        assertThat(result.getRecommendedProducts(), hasSize(1));
    }

    @Test
    @DisplayName("목표 상세 조회 - 존재하지 않는 목표로 예외 발생")
    void getGoalDetail_NotFound() {
        // Given
        String userId = "testUser";
        Long goalId = 999L;

        // 검증 서비스가 예외를 던지도록 설정
        when(validationService.validateGoalExists(userId, goalId))
                .thenThrow(new NoSuchElementException("요청하신 목표 (ID: 999)를 찾을 수 없습니다."));

        // When & Then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> goalService.getGoalDetail(userId, goalId)
        );

        assertThat(exception.getMessage(), containsString("요청하신 목표 (ID: 999)를 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("목표 생성 성공 - 모든 서비스가 올바르게 호출되는지 검증")
    void createGoal_Success() {
        // Given
        String userId = "testUser";
        GoalCreateRequestDTO request = GoalCreateRequestDTO.builder()
                .categoryId(1)
                .goalName("내 집 마련")
                .targetAmount(new BigDecimal("50000000"))
                .goalDurationMonths(24)
                .build();

        String mockFutureMessage = "미래의 나로부터 온 메시지";
        List<RecommendedProductVO> mockRecommendations = Collections.emptyList();

        // 서비스들의 동작 Mocking
        doNothing().when(validationService).validateCategoryExists(request.getCategoryId());
        when(messageService.generateFutureMessage(any(), any(), eq(request.getGoalName()), eq(request.getTargetAmount())))
                .thenReturn(mockFutureMessage);
        when(recommendationService.calculateRecommendations(request, 1L)).thenReturn(mockRecommendations);
        when(goalMapper.getRecommendedProductList(1L)).thenReturn(mockRecommendations);

        // GoalVO 캡처 및 ID 설정
        ArgumentCaptor<GoalVO> goalCaptor = ArgumentCaptor.forClass(GoalVO.class);
        doAnswer(invocation -> {
            GoalVO goal = invocation.getArgument(0);
            goal.setId(1L); // DB에 insert 후 생성된 ID 모방
            return null;
        }).when(goalMapper).createGoal(goalCaptor.capture());

        // Nowme 정보 Mocking
        when(goalMapper.getNowmeIdByUserId(userId)).thenReturn(1);

        // When
        GoalCreateResponseDTO result = goalService.createGoal(userId, request);

        // Then
        // 각 서비스가 정확한 인자와 함께 호출되었는지 검증
        verify(validationService).validateCategoryExists(request.getCategoryId());
        verify(messageService).generateFutureMessage(any(), any(), any(), any());
        verify(recommendationService).calculateRecommendations(eq(request), eq(1L));
        verify(goalMapper).createGoal(any(GoalVO.class));
        verify(goalMapper, never()).insertRecommendedProductList(any()); // 추천 상품이 없으므로 호출되지 않아야 함

        assertThat(result.getGoalId(), equalTo(1L));
        assertThat(result.getFutureMeMessage(), equalTo(mockFutureMessage));

        GoalVO savedGoal = goalCaptor.getValue();
        assertThat(savedGoal.getGoalName(), equalTo("내 집 마련"));
        assertThat(savedGoal.getStatus(), equalTo("DRAFT"));
    }

    @Test
    @DisplayName("목표 발행 성공")
    void publishAsReport_Success() {
        // Given
        String userId = "testUser";
        Long goalId = 1L;
        Long selectedProductId = 1001L;

        GoalDetailResponseDTO existingGoal = GoalDetailResponseDTO.builder().id(goalId).build();
        RecommendedProductVO selectedProduct = RecommendedProductVO.builder().productId(selectedProductId)
                .saveAmount(new BigDecimal("800000")).expectedTotalAmount(new BigDecimal("50000000")).build();
        GoalSummaryResponseDTO publishedGoal = GoalSummaryResponseDTO.builder().id(goalId).status("PUBLISHED").build();

        // 검증 서비스가 객체를 반환하도록 설정
        when(validationService.validateGoalExists(userId, goalId)).thenReturn(existingGoal);
        when(validationService.validateProductExists(selectedProductId, goalId)).thenReturn(selectedProduct);
        when(goalMapper.getGoalSummaryById(goalId)).thenReturn(publishedGoal);

        // When
        GoalSummaryResponseDTO result = goalService.publishAsReport(userId, goalId, selectedProductId);

        // Then
        assertThat(result.getStatus(), equalTo("PUBLISHED"));

        // DB 업데이트 메소드가 정확한 인자와 함께 호출되었는지 검증
        verify(goalMapper).updateGoalStatusToPublished(
                eq(goalId),
                eq(selectedProductId),
                eq(selectedProduct.getSaveAmount()),
                eq(selectedProduct.getExpectedTotalAmount())
        );
    }

    @Test
    @DisplayName("목표 달성 성공")
    void achieveGoal_Success() {
        // Given
        String userId = "testUser";
        Long goalId = 1L;

        GoalDetailResponseDTO existingGoal = GoalDetailResponseDTO.builder().id(goalId).build();
        GoalSummaryResponseDTO achievedGoal = GoalSummaryResponseDTO.builder().id(goalId).status("ACHIEVED").build();

        when(validationService.validateGoalExists(userId, goalId)).thenReturn(existingGoal);
        when(goalMapper.getGoalSummaryById(goalId)).thenReturn(achievedGoal);

        // When
        GoalSummaryResponseDTO result = goalService.achieveGoal(userId, goalId);

        // Then
        assertThat(result.getStatus(), equalTo("ACHIEVED"));
        verify(goalMapper).updateGoalStatusToAchieved(eq(goalId), any(LocalDateTime.class));
    }
}