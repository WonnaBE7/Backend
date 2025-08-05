package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.GoalDetailResponseDTO;

public interface GoalValidationService {
    public void validateGoalStatus(String status);

    public void validateCategoryExists(Integer categoryId);

    public GoalDetailResponseDTO validateGoalExists(String userId, Long goalId);

    public RecommendedProductVO validateProductExists(Long productId, Long goalId);

}
