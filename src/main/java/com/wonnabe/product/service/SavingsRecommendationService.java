package com.wonnabe.product.service;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO;

import java.util.Map;

public interface SavingsRecommendationService {
    SavingsRecommendationResponseDTO recommendSavings(String userId, int topN);

    double calculateScore(SavingsProductVO score, double[] weights);

    Map<Integer, double[]> getPersonaWeights();
}
