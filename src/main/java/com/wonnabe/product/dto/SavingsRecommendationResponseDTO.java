package com.wonnabe.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsRecommendationResponseDTO {

    private String userId;
    private List<PersonaRecommendation> recommendationsByPersona;

    // 각 페르소나별 추천 결과
    public static class PersonaRecommendation {
        private int personaId;
        private String personaName;
        private List<RecommendedSavings> products;
    }

    // 추천된 예적금 상품
    public static class RecommendedSavings {
        private String productId;
        private String productName;
        private String bankName;
        private double baseRate;
        private double maxRate;
        private double totalScore;
        // getter/setter
    }
}
