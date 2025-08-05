package com.wonnabe.product.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wonnabe.product.domain.enums.CardType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카드 상품에 대한 상세 정보를 조회하는 DTO입니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardProductDetailResponseDTO {
	private CardInfo cardInfo;
	private List<ComparisonChart> comparisonChart;
	private Note note;

	// 카드 상품 기본 정보
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CardInfo {
		private long cardId;
		private String cardName;
		private String cardCompany;
		private int matchScore;
		private String mainBenefit;
		private String cardType;
		private String benefitSummary;
		@JsonProperty("isWished")
		private boolean isWished;
		private List<String> labels;
		private List<Integer> currentUserData;
	}

	// 현재 상품과의 비교 정보
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ComparisonChart {
		private long compareId;
		private String compareName;
		private List<Integer> recommendedProductData;
	}

	// 유의 사항
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Note {
		private String category;
		private String previousMonthSpending;
		private String usage;
		private String annualFee;
	}
}
