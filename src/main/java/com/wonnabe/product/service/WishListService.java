package com.wonnabe.product.service;

import com.wonnabe.product.dto.WishListRequestDTO;
import com.wonnabe.product.dto.WishProductResponseDTO;

public interface WishListService {
	/**
	 * 사용자 아이디로 사용자의 선호 상품을 조회한다
	 * @param userId 사용자 아이디
	 * @return 사용자 선호 상품
	 */
	WishProductResponseDTO findWishProductByUserId(String userId);

	/**
	 * 사용자 관심 상품 추가 / 삭제
	 * @param userRequest 사용자 요청 사항
	 */
	void updateWishList(String userId, WishListRequestDTO userRequest);
}
