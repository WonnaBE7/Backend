package com.wonnabe.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceRecommendationResponseDTO {
    private String userId;
    private List<PersonaRecommendation> recommendationsByPersona;

    // 각 페르소나별 추천결과
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonaRecommendation {
        private Integer personaId;
        private String personaName;
        private List<RecommendedInsurance> products;
    }

    // 추천된 보험 상품
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedInsurance {
        private String productId;
        private String productName;
        private String providerName;
        private String productType;

        private String coverageLimit;
        private String note;
        private String myMoney;
        private double totalScore;
    }
}
