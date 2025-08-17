package com.wonnabe.product.service;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO;

import java.util.Map;

public interface SavingsRecommendationService {
    SavingsRecommendationResponseDTO recommendSavings(String userId, int topN);
    Map<Integer, double[]> getPersonaWeights();
    double calculateScore(SavingsProductVO score, double[] weights);
    double[] adjustWeightsByIncome(double[] weights, String incomeSource, String employment);
    double[] normalizeWeights(double[] weights);
}

