package com.wonnabe.product.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicUserInfoDTO {
	private String userId;
	private String favoriteProductsByType;
	private Long incomeAnnualAmount;
	private Integer nowMeId;
	private Double previousConsumption;
	private String incomeSourceType;
	private String incomeEmploymentStatus;
	private String smokingStatus;
	private String familyMedicalHistory;
	private String pastMedicalHistory;
	private String exerciseFrequency;
	private String drinkingFrequency;

	// 내 카드 Id 리스트로 변환
	public List<Long> getMyFavorite() {
		// 예: ["1", "2"] → List<Long>로 변환
		String ids = favoriteProductsByType.replaceAll("[\\[\\]\\s\"]", ""); // 대괄호, 공백, 쌍따옴표 제거
		return Arrays.stream(ids.split(","))
			.filter(s -> !s.isEmpty())
			.map(Long::parseLong)
			.collect(Collectors.toList());
	}
}
