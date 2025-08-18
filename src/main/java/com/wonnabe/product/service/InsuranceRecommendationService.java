package com.wonnabe.product.service;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.dto.InsuranceRecommendationResponseDTO;

import java.util.Map;

public interface InsuranceRecommendationService {
    InsuranceRecommendationResponseDTO recommendInsurance(String userId, int topN);
    Map<Integer, double[]> getPersonaWeights();
    double calculateScore(InsuranceProductVO product, double[] weights);
    Map<String, Double> adjustWeightsByHealthAndLifestyle(Map<String, Double> weights, int smokingStatus, int familyMedicalHistory, int pastMedicalHistory, int exerciseFrequency, int drinkingFrequency);
    Map<String, Double> normalizeWeights(Map<String, Double> weights);
    double[] convertWeightsMapToArray(Map<String, Double> weightsMap);
}
