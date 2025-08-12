package com.wonnabe.product.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishProductResponseDTO {

	int totalCount;
	List<Savings> savings; // 예적금
	List<Card> cards; // 카드
	List<Insurance> insurances; // 보험

	// 예적금
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Savings {
		private String productType;
		private String productId;
		private String productName;
		private String bankName;
		private double baseRate;
		private double maxRate;
		private double score;
	}

	// 카드
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Card {
		public String productType;
		private String cardId;
		private String cardName;
		private String cardCompany;
		private String cardType;
		private double score;
		private String mainBenefit;
		private String annualFeeDomestic;
		private String annualFeeOverseas;
	}

	// 보험
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Insurance {
		private String productType;
		private String productId;
		private String productName;
		private String providerName;
		private String coverageType;
		private String coverageLimit;
		private double score;
	}
}
