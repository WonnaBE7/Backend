package com.wonnabe.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
	long productId; // 상품 아이디
	String type; // 상품 종류
	String productName; // 상품명
	String description; // 간단한 설명

	// 긴 설명을 간단하게 변경
	public void makeSimpleDescription() {
		if (description.equals("도수치료/체외충격파/증식치료")) {
			description = "도수치료";
		} else if (description.equals("자기공명영상진단(MRI/MRA)")) {
			description = "자기공명영상진단";
		}
	}
}


