package com.wonnabe.product.mapper;

import static com.wonnabe.product.dto.ProductResponseDTO.*;

import java.util.List;


public interface ProductMapper {
	/**
	 * 사용자 카드 요약 조회
	 * @param userId 사용자 아이디
	 * @return 사용자 카드 요약 목록
	 */
	List<Card> findCardSummaryByUserId(String userId);

	/**
	 * 사용자 보험 요약 조회
	 * @param userId 사용자 아이디
	 * @return 사용자 보험 요약 목록
	 */
	List<Insurance> findInsuranceSummaryByUserId(String userId);

	/**
	 * 사용자 예적금 요약 조회
	 * @param userId 사용자 아이디
	 * @return 사용자 예적금 요약 목록
	 */
	List<Savings> findDepositSummaryByUserId(String userId);
}
