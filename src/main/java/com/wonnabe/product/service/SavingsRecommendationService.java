package com.wonnabe.product.service;

public interface SavingsRecommendationService {
    SavingsRecommendationResponseDto recommendSavings(String userId, int topN);
}
