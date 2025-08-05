package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.GoalVO;
import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.*;
import com.wonnabe.goal.mapper.GoalMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service("goalServiceImpl")
@RequiredArgsConstructor
@Log4j2
public class GoalServiceImpl implements GoalService {

    private final GoalMapper goalMapper;
    private final ProductRecommendationService recommendationService;
    private final FutureMessageService messageService;
    private final GoalValidationService validationService;

    @Override
    public GoalListResponseDTO getGoalList(String userId, String status) {
        validationService.validateGoalStatus(status);

        List<GoalSummaryResponseDTO> goalSummaries = goalMapper.getGoalList(userId, status);
        BigDecimal totalTargetAmount = calculateTotalTargetAmount(goalSummaries);

        return GoalListResponseDTO.builder()
                .totalGoalCount(goalSummaries.size())
                .totalTargetAmount(totalTargetAmount)
                .goals(goalSummaries)
                .build();
    }

    @Override
    public GoalDetailResponseDTO getGoalDetail(String userId, Long goalId) {
        GoalDetailResponseDTO goalDetail = validationService.validateGoalExists(userId, goalId);

        List<RecommendedProductDTO> recommendedProducts =
                RecommendedProductDTO.ofList(goalMapper.getRecommendedProductList(goalId));
        goalDetail.setRecommendedProducts(recommendedProducts);

        return goalDetail;
    }

    @Override
    @Transactional
    @SneakyThrows
    public GoalCreateResponseDTO createGoal(String userId, GoalCreateRequestDTO request) {
        validationService.validateCategoryExists(request.getCategoryId());

        NowmeInfo nowmeInfo = getNowmeInfo(userId);
        String futureMeMessage = messageService.generateFutureMessage(
                nowmeInfo.getName(), nowmeInfo.getDescription(),
                request.getGoalName(), request.getTargetAmount()
        );

        GoalVO goalToInsert = buildGoalVO(userId, request, nowmeInfo.getId(), futureMeMessage);
        goalMapper.createGoal(goalToInsert);
        Long createdGoalId = goalToInsert.getId();

        List<RecommendedProductVO> recommendations =
                recommendationService.calculateRecommendations(request, createdGoalId);

        if (!recommendations.isEmpty()) {
            goalMapper.insertRecommendedProductList(recommendations);
        }

        List<RecommendedProductDTO> recommendedProductList =
                RecommendedProductDTO.ofList(goalMapper.getRecommendedProductList(createdGoalId));

        return GoalCreateResponseDTO.builder()
                .goalId(createdGoalId)
                .futureMeMessage(futureMeMessage)
                .recommendedProducts(recommendedProductList)
                .build();
    }

    @Override
    @Transactional
    public GoalSummaryResponseDTO publishAsReport(String userId, Long goalId, Long selectedProductId) {
        validationService.validateGoalExists(userId, goalId);
        RecommendedProductVO selectedProduct = validationService.validateProductExists(selectedProductId, goalId);

        goalMapper.updateGoalStatusToPublished(goalId, selectedProductId,
                selectedProduct.getSaveAmount(), selectedProduct.getExpectedTotalAmount());

        return goalMapper.getGoalSummaryById(goalId);
    }

    @Override
    @Transactional
    public GoalSummaryResponseDTO achieveGoal(String userId, Long goalId) {
        validationService.validateGoalExists(userId, goalId);
        goalMapper.updateGoalStatusToAchieved(goalId, LocalDateTime.now());
        return goalMapper.getGoalSummaryById(goalId);
    }

    private BigDecimal calculateTotalTargetAmount(List<GoalSummaryResponseDTO> goalSummaries) {
        return goalSummaries.stream()
                .map(goal -> goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private NowmeInfo getNowmeInfo(String userId) {
        Integer nowmeId = goalMapper.getNowmeIdByUserId(userId);
        if (nowmeId == null) {
            return new NowmeInfo(null, null, null);
        }

        String nowmeName = goalMapper.getNowmeNameByNowmeId(nowmeId);
        String nowmeDescription = goalMapper.getNowmeDescriptionByNowmeId(nowmeId);
        return new NowmeInfo(nowmeId, nowmeName, nowmeDescription);
    }

    private GoalVO buildGoalVO(String userId, GoalCreateRequestDTO request, Integer nowmeId, String futureMeMessage) {
        return GoalVO.builder()
                .userId(userId)
                .nowmeId(nowmeId)
                .categoryId(request.getCategoryId())
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .goalDurationMonths(request.getGoalDurationMonths())
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(request.getGoalDurationMonths()))
                .currentAmount(BigDecimal.valueOf(0))
                .progressRate(BigDecimal.valueOf(0))
                .isAchieved(false)
                .resultSummary(futureMeMessage)
                .status("DRAFT")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Getter
    @AllArgsConstructor
    private static class NowmeInfo {
        private final Integer id;
        private final String name;
        private final String description;
    }
}
