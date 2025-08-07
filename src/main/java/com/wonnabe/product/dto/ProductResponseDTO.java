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
public class ProductResponseDTO {

	DepositsWithCount deposits; // 예적금
	CardWithCount cards; // 카드
	InsuranceWithCount insurances; // 보험

	// 예적금
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Deposit {
		private String productId;
		private String productName;
		private double interestRate;
	}



	// 카드
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Card {
		private String cardId;
		private String cardName;
		private String benefitDescription;
	}

	// 보험
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Insurance {
		private String productId;
		private String insuranceName;
		private String coverage;
	}

	// 예적금
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DepositsWithCount {
		int count;
		public List<Deposit> products;
	}

	// 카드
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CardWithCount {
		int count;
		public List<Card> products;
	}

	// 보험
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class InsuranceWithCount {
		int count;
		public List<Insurance> products;
	}

	// 긴 설명을 간단하게 변경
	public void makeSimpleDescription() {
		for (Insurance insurance : insurances.products) {
			if (insurance.coverage.equals("도수치료/체외충격파/증식치료")) {
				insurance.coverage = "도수치료";
			} else if (insurance.coverage.equals("자기공명영상진단(MRI/MRA)")) {
				insurance.coverage = "자기공명영상진단";
			}
		}
	}
}
