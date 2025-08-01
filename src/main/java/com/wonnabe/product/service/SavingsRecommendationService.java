package com.wonnabe.product.service;

import com.wonnabe.product.dto.SavingsRecommendationResponseDTO;

public interface SavingsRecommendationService {
    SavingsRecommendationResponseDTO recommendSavings(String userId, int topN);
}
