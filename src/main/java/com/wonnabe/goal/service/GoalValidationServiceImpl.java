package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.GoalDetailResponseDTO;
import com.wonnabe.goal.mapper.GoalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GoalValidationServiceImpl implements GoalValidationService {

    private final GoalMapper goalMapper;

    @Override
    public void validateGoalStatus(String status) {
        if (!Arrays.asList("PUBLISHED", "ACHIEVED").contains(status)) {
            throw new IllegalArgumentException("유효하지 않은 상태 값입니다. (허용: PUBLISHED, ACHIEVED)");
        }
    }

    @Override
    public void validateCategoryExists(Integer categoryId) {
        String categoryName = goalMapper.getCategoryNameById(categoryId);
        if (categoryName == null) {
            throw new IllegalArgumentException("해당하는 카테고리가 존재하지 않습니다.");
        }
    }

    @Override
    public GoalDetailResponseDTO validateGoalExists(String userId, Long goalId) {
        GoalDetailResponseDTO goal = goalMapper.getGoal(userId, goalId);
        if (goal == null) {
            throw new NoSuchElementException("요청하신 목표 (ID: " + goalId + ")를 찾을 수 없습니다.");
        }
        return goal;
    }

    @Override
    public RecommendedProductVO validateProductExists(Long productId, Long goalId) {
        RecommendedProductVO product = goalMapper.findRecommendedProductById(productId, goalId);
        if (product == null) {
            throw new NoSuchElementException("해당하는 상품이 없습니다.");
        }
        return product;
    }
}
