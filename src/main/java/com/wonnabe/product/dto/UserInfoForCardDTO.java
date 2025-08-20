package com.wonnabe.product.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카드 추천에 필요한 사용자 정보를 가져오는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoForCardDTO {
	private String userId; // 사용자 id
	private Double incomeAnnualAmount; // 연소득
	private String selectedWonnabeIds; // 내가 선택한 워너비 아이디
	private String myCardIds; // 내가 보유한 카드 목록
	private Double previousConsumption; // 전월 소비량

	// 페르소나 ID 리스트로 변환
	public List<Integer> getPersonaIds() {
		// 예: ["1", "2"] → List<Integer>로 변환
		String ids = selectedWonnabeIds.replaceAll("[\\[\\]\\s\"]", ""); // 대괄호, 공백, 쌍따옴표 제거
		return Arrays.stream(ids.split(","))
			.filter(s -> !s.isEmpty())
			.map(Integer::parseInt)
			.collect(Collectors.toList());
	}

	// 내 카드 Id 리스트로 변환
	public List<Long> getMyCardIds() {
		// 예: ["1", "2"] → List<Long>로 변환
		String ids = myCardIds.replaceAll("[\\[\\]\\s\"]", ""); // 대괄호, 공백, 쌍따옴표 제거
		return Arrays.stream(ids.split(","))
			.filter(s -> !s.isEmpty())
			.map(Long::parseLong)
			.collect(Collectors.toList());
	}
}
