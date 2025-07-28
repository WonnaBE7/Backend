package com.wonnabe.goal.service;

import com.wonnabe.goal.dto.GoalDetailResponseDTO;
import com.wonnabe.goal.dto.GoalListResponseDTO;
import com.wonnabe.goal.dto.GoalSummaryResponseDTO;
import com.wonnabe.goal.dto.RecommendedProductDTO;
import com.wonnabe.goal.mapper.GoalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    final private GoalMapper goalMapper;

    @Override
    public GoalListResponseDTO getGoalList(UUID userId) {
        List<GoalSummaryResponseDTO> goalSummaries = goalMapper.getGoalList(userId.toString());

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
    public GoalDetailResponseDTO getGoalDetail(UUID userId, Long goalId) {
        GoalDetailResponseDTO goalDetail = goalMapper.getGoal(userId.toString(), goalId);

        List<RecommendedProductDTO> recommendedProducts = new ArrayList<>(); // TODO: 추천된 리스트 가져오는 함수 필요
        goalDetail.setRecommendedProducts(recommendedProducts);

        return goalDetail;
    }
}
