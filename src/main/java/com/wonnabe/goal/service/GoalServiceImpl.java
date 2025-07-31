package com.wonnabe.goal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.goal.domain.GoalVO;
import com.wonnabe.goal.dto.*;
import com.wonnabe.goal.mapper.GoalMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service("goalServiceImpl")
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalMapper goalMapper;
    private final ObjectMapper objectMapper;

    @Override
    public GoalListResponseDTO getGoalList(String userId, String status) {
        // status 유효성 검사
        if (!status.equals("PUBLISHED") && !status.equals("ACHIEVED")) {
            throw new IllegalArgumentException("유효하지 않은 status 값입니다. (허용: PUBLISHED, ACHIEVED)");
        }

        List<GoalSummaryResponseDTO> goalSummaries = goalMapper.getGoalList(userId, status);

        BigDecimal totalTargetAmount = goalSummaries.stream()
                .map(goal -> goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return GoalListResponseDTO.builder()
                .totalGoalCount(goalSummaries.size())
                .totalTargetAmount(totalTargetAmount)
                .goals(goalSummaries)
                .build();
    }

    @Override
    public GoalDetailResponseDTO getGoalDetail(String userId, Long goalId) {
        GoalDetailResponseDTO goalDetail = goalMapper.getGoal(userId, goalId);

        // 목표 존재하지 않는 예외
        if (goalDetail == null) {
            throw new NoSuchElementException("요청하신 목표 (ID: " + goalId + ")를 찾을 수 없습니다.");
        }

        List<RecommendedProductDTO> recommendedProducts = RecommendedProductDTO.ofList(goalMapper.getRecommendedProductList(goalId));
        goalDetail.setRecommendedProducts(recommendedProducts);

        return goalDetail;
    }

    @Override
    @Transactional
    @SneakyThrows
    public GoalCreateResponseDTO createGoal(String userId, GoalCreateRequestDTO request) {
        // 목표 기간 0 이하 예외
        if (request.getGoalDurationMonths() <= 0) {
            throw new IllegalArgumentException("목표 기간(goalDurationMonths)은 1 이상이어야 합니다.");
        }

        // nowmeId 조회
        Integer nowmeId = goalMapper.getNowmeIdByUserId(userId);
        String nowmeName = null;
        if (nowmeId != null) {
            nowmeName = goalMapper.getNowmeNameByNowmeId(nowmeId);
        }

        // 월 저축액 계산
        // TODO: 예금/적금인지에 따라 계산 로직 달라지게 하기
        BigDecimal monthlySaveAmount = request.getTargetAmount()
                .divide(new BigDecimal(request.getGoalDurationMonths()), 0, RoundingMode.UP);

        // 추천 상품 목록 조회 및 이자 계산 로직
        // TODO: 실제 가져오기
        List<RecommendedProductDTO> recommendedProductList = Arrays.asList(
                RecommendedProductDTO.builder()
                        .id(1001L)
                        .name("하나 예금 상품")
                        .bank("하나은행")
                        .interestRate(BigDecimal.valueOf(3.5))
                        .achievementRate(80)
                        .monthlyDepositAmount(monthlySaveAmount)
                        .expectedAchievementDate(LocalDate.now().plusMonths(request.getGoalDurationMonths()))
                        .expectedTotalAmount(monthlySaveAmount.multiply(new BigDecimal(request.getGoalDurationMonths())))
                        .build(),

                RecommendedProductDTO.builder()
                        .id(1010L)
                        .name("카카오 적금 상품")
                        .bank("카카오뱅크")
                        .interestRate(BigDecimal.valueOf(3.0))
                        .achievementRate(70)
                        .monthlyDepositAmount(monthlySaveAmount)
                        .expectedAchievementDate(LocalDate.now().plusMonths(request.getGoalDurationMonths()))
                        .expectedTotalAmount(monthlySaveAmount.multiply(new BigDecimal(request.getGoalDurationMonths())))
                        .build()
        );

        // 예상 이자 및 총 수령액 계산
        // TODO: 실제 계산식 필요
        BigDecimal expectedTotalAmount = monthlySaveAmount.multiply(new BigDecimal(request.getGoalDurationMonths()));
        BigDecimal interestGain = expectedTotalAmount.multiply(BigDecimal.valueOf(0.03)).setScale(0, RoundingMode.DOWN); // 단순 3% 수익률

        // 미래의 나에게 보내는 메시지 생성
        String futureMeMessage = generateFutureMeMessage(nowmeName);

        // 추천 상품 ID 목록을 List<Long>으로 추출
        // TODO: 실제 예적금 추천

        GoalVO goalToInsert = GoalVO.builder()
                .userId(userId)
                .nowmeId(nowmeId)
                .categoryId(request.getCategoryId())
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .goalDurationMonths(request.getGoalDurationMonths())
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(request.getGoalDurationMonths()))
                .monthlySaveAmount(monthlySaveAmount)
                .expectedTotalAmount(expectedTotalAmount)
                .interestGain(interestGain)
                .progressRate(BigDecimal.valueOf(0))
                .isAchieved(false)
                .resultSummary(futureMeMessage)
                .status("DRAFT")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Mapper를 통해 DB 저장
        goalMapper.createGoal(goalToInsert);

        // 생성된 goalId 조회
        Long createdGoalId = goalToInsert.getId();

        // 반환 DTO 생성
        return GoalCreateResponseDTO.builder()
                .goalId(createdGoalId)
                .futureMeMessage(futureMeMessage)
                .recommendedProducts(recommendedProductList)
                .build();
    }

    @Override
    @Transactional
    public GoalSummaryResponseDTO publishAsReport(String userId, Long goalId, Long selectedProductId) {
        // 목표 존재 확인
        GoalDetailResponseDTO existingGoal = goalMapper.getGoal(userId, goalId);
        if (existingGoal == null) {
            throw new NoSuchElementException("목표 (ID: " + goalId + ")를 찾을 수 없습니다.");
        }

        goalMapper.updateGoalStatusToPublished(goalId, selectedProductId);

        return goalMapper.getGoalSummaryById(goalId);
    }

    @Override
    @Transactional
    public GoalSummaryResponseDTO achieveGoal(String userId, Long goalId) {
        // 목표 존재 확인
        GoalDetailResponseDTO existingGoal = goalMapper.getGoal(userId, goalId);
        if (existingGoal == null) {
            throw new NoSuchElementException("목표 (ID: " + goalId + ")를 찾을 수 없습니다.");
        }

        goalMapper.updateGoalStatusToAchieved(goalId, LocalDateTime.now());

        return goalMapper.getGoalSummaryById(goalId);
    }

    private String generateFutureMeMessage(String nowmeName) {
        // TODO: GPT API
        if (nowmeName == null) return "지금의 당신도 멋져요!";
        return nowmeName + "님, 목표를 꼭 달성하고 미래의 나에게 칭찬을 아끼지 마세요!";
    }
}
