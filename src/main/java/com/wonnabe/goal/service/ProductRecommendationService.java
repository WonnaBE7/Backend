package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.GoalCreateRequestDTO;

import java.util.List;

public interface ProductRecommendationService {
    public List<RecommendedProductVO> calculateRecommendations(GoalCreateRequestDTO request, Long goalId);
}
