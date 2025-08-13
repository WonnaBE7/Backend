package com.wonnabe.product.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카드 상품 추천 결과를 반환하기 위한 DTO 입니다
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRecommendationResponseDTO {
	private String userId;
	private List<CardRecommendationResponseDTO.PersonaRecommendation> recommendationsByPersona;

	// 각 페르소나별 추천 결과
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PersonaRecommendation {
		private int personaId;
		private String personaName;
		private List<CardRecommendationResponseDTO.RecommendedCard> products;
	}

	// 추천된 카드 상품
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RecommendedCard {
		private String productType;
		private String cardId;
		private String cardName;
		private String cardCompany;
		private String cardType;
		private double score;
		private String mainBenefit;
		private String annualFeeDomestic;
		private String annualFeeOverSeas;
	}
}
